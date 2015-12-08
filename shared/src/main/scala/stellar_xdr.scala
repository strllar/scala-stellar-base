package org.strllar.stellarbase

package object manual_xdr {
  //todo: support default in union switch
  //todo: support polymorphic setter $[T](x :T)
  //todo: support getter and polymorphic $[T]:T
  //todo: totally mock code, need rewrite
  //todo: accurate varlen and fixlen
  //todo: accurate signed and unsigned

  type RawOpaque = Vector[Byte]

  trait XDRStagedItem {
    def toOpaque :RawOpaque
  }

  //intrinsic xdr type defines
  type void = Unit
  //type defines in .xdr files
  type uint32 = Int
  type uint64 = Long
  type int64 = Long
  type SequenceNumber = uint64
  val AccountID = PublicKey

}

package manual_xdr {

  object BytesPacker {
    def apply(x :Int) :RawOpaque = Vector[Byte](((x >>> 24) & 0xff).toByte, ((x >>> 16) & 0xff).toByte, ((x >>> 8) & 0xff).toByte, (x & 0xff).toByte)

    def apply(x :Long) :RawOpaque  = Vector[Byte](((x >>> 56) & 0xff).toByte, ((x >>> 48) & 0xff).toByte, ((x >>> 40) & 0xff).toByte, (x >>>32 & 0xff).toByte,
      ((x >>> 24) & 0xff).toByte, ((x >>> 16) & 0xff).toByte, ((x >>> 8) & 0xff).toByte, (x & 0xff).toByte)

    def apply(x :String) :RawOpaque = {
      val newlen = (x.length + 3) & ~3
      BytesPacker(x.length) ++ x.getBytes.toSeq.padTo(newlen, 0 toByte).toVector
    }
  }

  import shapeless._
  import shapeless.ops.coproduct.{Mapper, Unifier}
  import shapeless.ops.hlist.LeftFolder

  object concatOp extends Poly2 {
    implicit  def default[T](implicit xo: Lazy[XDROpaque.Case.Aux[T, RawOpaque]]) =
      at[RawOpaque, T]{ (acc, t) => acc ++ xo.value(t) }
  }
  object XDROpaque extends Poly1 {

    //intrinsic XDR rules

    //implicit def caseInt32 = at[uint32](x => Vector.empty[Byte])
    implicit def caseInt = at[Int](x => BytesPacker(x))

    implicit def caseLong = at[Long](x => BytesPacker(x))

    implicit def caseVector[T](implicit xo: Lazy[XDROpaque.Case.Aux[T, RawOpaque]]) = at[Vector[T]](x => BytesPacker(x.length) ++ x.flatMap(xo.value(_)))

    implicit def caseString = at[String](x => BytesPacker(x))

    implicit def caseOption[T](implicit xo: Lazy[XDROpaque.Case.Aux[T, RawOpaque]]) = at[Option[T]]({
      case None => Vector[Byte](0,0,0,0)
      case Some(x) => Vector[Byte](0,0,0,1) ++ xo.value(x)
    })

    implicit def caseOpaque = at[RawOpaque](identity)
    //implicit def caseNone = at[None.type](x => Vector[Byte](0,0,0,0))
    //implicit def caseSome[T](implicit xo: XDROpaque.Case.Aux[T, XDROpaque]) = at[Some[T]](x => Vector[Byte](1,0,0,0) ++ xo(x.get))

    implicit def caseProduct[T <:Product, L <: HList]
    (implicit gen :Generic.Aux[T, L], xo: Lazy[XDROpaque.Case.Aux[L, RawOpaque]], folder :LeftFolder.Aux[L, RawOpaque, concatOp.type, RawOpaque])
    = at[T]((x:T) => xo.value(gen.to(x)))

    implicit def caseCoProduct[T <:Coproduct, M <:Coproduct] (implicit mapper :Mapper.Aux[XDROpaque.type, T, M], un :Unifier[M]) = at[T](co => co.map(XDROpaque).unify)

    implicit def  caseHList[L <: HList](implicit folder :LeftFolder.Aux[L, RawOpaque, concatOp.type, RawOpaque]) = at[L]((l :L) => (l.foldLeft(Vector.empty[Byte])(concatOp) :RawOpaque))

    implicit def caseAny[T](implicit st: T => XDRStagedItem) = at[T]((x:T) => st(x).toOpaque)

    def from[T](x :T)(implicit xo: Lazy[XDROpaque.Case.Aux[T, RawOpaque]]) = xo.value(x)
  }

  case class uint256(opaqueN32 :RawOpaque)
  case class Hash(opaqueN32 :RawOpaque)
  case class SignatureHint(opaqueN4 :RawOpaque)
  object Signature {
    def apply(opaqueM64 :RawOpaque) = new Signature(opaqueM64)

    implicit def toOpaque(sig :Signature) = new XDRStagedItem {
      override def toOpaque: RawOpaque = XDROpaque.from(sig.opaqueM64.length, sig.opaqueM64)
    }
  }
  class Signature(val opaqueM64 :RawOpaque)
  case class TimeBounds(minTime :uint64,maxTime :uint64)

  object EnvelopeType
  {
    abstract class Enum(val value :Int)
    case object ENVELOPE_TYPE_SCP extends Enum(1)
    case object ENVELOPE_TYPE_TX extends Enum(2)
    case object ENVELOPE_TYPE_AUTH extends Enum(3)
  }

  object CryptoKeyType {
    abstract class Enum(val value :Int)
    case object KEY_TYPE_ED25519 extends Enum(0)
  }

  object PublicKey {
    trait Arm extends XDRStagedItem {
      val `type` :CryptoKeyType.Enum
    }
    class  arm_KEY_TYPE_ED25519(val ed25519 :uint256) extends Arm{
      val `type` = CryptoKeyType.KEY_TYPE_ED25519
      override  def toOpaque = XDROpaque.from(`type`.value, ed25519)
    }

    type Union = arm_KEY_TYPE_ED25519 :+: CNil

    def $KEY_TYPE_ED25519(ed25519 :uint256) = Coproduct[Union](new arm_KEY_TYPE_ED25519(ed25519))
  }

  object MemoType {
    abstract class Enum(val value :Int)
    case object MEMO_NONE extends Enum(0)
    case object MEMO_TEXT extends Enum(1)
    case object MEMO_ID extends Enum(2)
    case object MEMO_HASH extends Enum(3)
    case object MEMO_RETURN extends Enum(4)
  };

  object Memo {
    trait Arm extends XDRStagedItem {
      val `type` :MemoType.Enum
    }
    class  arm_MEMO_NONE() extends Arm  {
      val `type` = MemoType.MEMO_NONE
      override  def toOpaque = XDROpaque.from(`type`.value)
    }
    class arm_MEMO_TEXT(val text :String) extends Arm {
      val `type` = MemoType.MEMO_TEXT
      override  def toOpaque = XDROpaque.from(`type`.value, text)
    }
    class arm_MEMO_ID(val id :uint64) extends Arm {
      val `type` = MemoType.MEMO_ID
      override  def toOpaque = XDROpaque.from(`type`.value, id)
    }
    class  arm_MEMO_HASH(val hash :Hash) extends Arm {
      val `type` = MemoType.MEMO_HASH
      override  def toOpaque = XDROpaque.from(`type`.value, hash)
    }
    class arm_MEMO_RETURN(val retHash :Hash) extends Arm {
      val `type` = MemoType.MEMO_RETURN
      override  def toOpaque = XDROpaque.from(`type`.value, retHash)
    }

    type Union = arm_MEMO_NONE :+: arm_MEMO_TEXT :+: arm_MEMO_ID :+: arm_MEMO_HASH :+: arm_MEMO_RETURN :+: CNil

    def $MEMO_NONE() = Coproduct[Union](new arm_MEMO_NONE())
    def $MEMO_TEXT(text :String) = Coproduct[Union](new arm_MEMO_TEXT(text))
    def $MEMO_ID(id :uint64) = Coproduct[Union](new arm_MEMO_ID(id))
    def $MEMO_HASH(hash :Hash) = Coproduct[Union](new arm_MEMO_HASH(hash))
    def $MEMO_RETURN(retHash :Hash) = Coproduct[Union](new arm_MEMO_RETURN(retHash))
  }

  object OperationType {
    abstract class Enum(val value :Int)
    case object CREATE_ACCOUNT  extends Enum( 0 )
    case object PAYMENT  extends Enum( 1 )
    case object PATH_PAYMENT  extends Enum( 2 )
    case object MANAGE_OFFER  extends Enum( 3 )
    case object CREATE_PASSIVE_OFFER  extends Enum( 4 )
    case object SET_OPTIONS  extends Enum( 5 )
    case object CHANGE_TRUST  extends Enum( 6 )
    case object ALLOW_TRUST  extends Enum( 7 )
    case object ACCOUNT_MERGE  extends Enum( 8 )
    case object INFLATION  extends Enum( 9 )
  }

  case class CreateAccountOp(destination :AccountID.Union, startingBalance :int64)

  //case class PaymentOp(destination :AccountID, asset :Asset, amount :int64) //todo Asset
  case class PaymentOp(destination :AccountID.Union, amount :int64)

  object Operation {
    object Union_body {
      trait Arm extends XDRStagedItem {
        val `type` :OperationType.Enum
      }
      class arm_CREATE_ACCOUNT(val createAccountOp :CreateAccountOp) extends Arm {
        val `type` = OperationType.CREATE_ACCOUNT
        override def toOpaque = XDROpaque.from(`type`.value, createAccountOp)
      }
      class arm_PAYMENT(val paymentOp :PaymentOp) extends Arm {
        val `type` = OperationType.PAYMENT
        override def toOpaque = XDROpaque.from(`type`.value, paymentOp)
      }
      class arm_ACCOUNT_MERGE(val destination :AccountID.Union) extends Arm {
        val `type` = OperationType.ACCOUNT_MERGE
        override def toOpaque = XDROpaque.from(`type`.value, destination)
      }
      class arm_INFLATION() extends Arm {
        val `type` = OperationType.INFLATION
        override def toOpaque = XDROpaque.from(`type`.value)
      }

      type Union = arm_CREATE_ACCOUNT :+: arm_PAYMENT :+: arm_ACCOUNT_MERGE :+: arm_INFLATION :+: CNil

      def $CREATE_ACCOUNT(createAccountOp :CreateAccountOp) = Coproduct[Union](new arm_CREATE_ACCOUNT(createAccountOp))
      def $PAYMENT(paymentOp :PaymentOp) = Coproduct[Union](new arm_PAYMENT(paymentOp))
      def $ACCOUNT_MERGE(destination :AccountID.Union) = Coproduct[Union](new arm_ACCOUNT_MERGE(destination))
      def $INFLATION() = Coproduct[Union](new arm_INFLATION())
    }

    case class Components(body :Operation.Union_body.Union, sourceAccount :Option[AccountID.Union])


    def apply(body :Operation.Union_body.Union) = new Operation(Components(body, None))

    implicit def toOpaque(* :Operation) =  new XDRStagedItem {
      import *.*._
      override def toOpaque = XDROpaque.from(sourceAccount, body)
    }
  }

  class Operation(val * :Operation.Components) {
    def $sourceAccount(sourceAccount :AccountID.Union) = new Operation(*.copy(sourceAccount = Some(sourceAccount)))
  }

  object Transaction {
    object Union_ext {
      abstract class Enum(val value :Int)
      case object _0 extends Enum(0)

      trait Arm extends XDRStagedItem {
        val v :Enum
      }
      class arm_0() extends Arm {
        val v = _0
        override def toOpaque = XDROpaque.from(v.value)
      }
      type Union = arm_0 :+: CNil

      def $0() = Coproduct[Union](new arm_0())

    }
    case class Components(sourceAccount :AccountID.Union, fee :uint32, seqNum :SequenceNumber, memo :Memo.Union, ext :Transaction.Union_ext.Union, timeBounds :Option[TimeBounds], operations :Vector[Operation] )

    def apply(sourceAccount :AccountID.Union, fee :uint32, seqNum :SequenceNumber, memo :Memo.Union, ext :Transaction.Union_ext.Union)
    = new Transaction(Components(sourceAccount, fee , seqNum, memo, ext, None, Vector.empty[Operation]))

    implicit def toOpaque(* :Transaction) =  new XDRStagedItem {
      import *.*._
      override def toOpaque = XDROpaque(sourceAccount :: fee :: seqNum :: timeBounds :: memo :: operations :: ext :: HNil)
    }
  }

  class Transaction(val * :Transaction.Components) {
    def $timeBounds(x :TimeBounds) = new Transaction(*.copy(timeBounds = Some(x)))
    def $operations(x :Operation*) = new Transaction(*.copy(operations = x.toVector))
  }

  case class DecoratedSignature(hint :SignatureHint, signature :Signature)

  object TransactionEnvelope {
    case class Components(tx :Transaction, signatures :Vector[DecoratedSignature])
    def apply(tx :Transaction) = new TransactionEnvelope(Components(tx, Vector.empty[DecoratedSignature]))
    implicit def toOpaque(* :TransactionEnvelope) =  new XDRStagedItem {
      import *.*._
      override def toOpaque = XDROpaque.from(tx, signatures)
    }
  }

  class TransactionEnvelope(val * :TransactionEnvelope.Components) {
    def $signatures(signatures :DecoratedSignature*) = new TransactionEnvelope(*.copy(signatures = signatures.toVector))
  }

}

package stellar_xdr {
import manual_xdr._

object ops {

  implicit def accountid2xdr(acct :org.strllar.stellarbase.StrAccountID) :AccountID.Union = AccountID.$KEY_TYPE_ED25519(uint256(acct.rawbytes.toVector))

  implicit def AccountIDOPs(acct :AccountID.Union) = new {
    def hint :SignatureHint = {
      SignatureHint(acct.select[AccountID.arm_KEY_TYPE_ED25519].get.ed25519.opaqueN32.takeRight(4).toVector)
    }
  }

  implicit def TransactionOPs(tx :Transaction) = new {
    def hashid(implicit network :Network) = { //:Hash256
      val md256 = new Sha256()
      md256.update(XDROpaque.from(network.networkId, EnvelopeType.ENVELOPE_TYPE_TX.value, tx))
      md256.digest
    }
  }

  implicit def  AccountOP(seed :StrSeed) = new {
    def signTx(tx :Transaction)(implicit network :Network) = {
      val acct :AccountID.Union = seed.accountid
      DecoratedSignature(acct.hint, Signature(seed.sign(tx.hashid).toVector))
    }
  }

}

}