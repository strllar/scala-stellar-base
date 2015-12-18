package org.strllar.stellarbase

import scodec.{Codec, Attempt, Err, codecs}
import org.strllar.scalaxdr.xdrbase.XDRCodecs._

package object manual_xdr {
  //todo: support default in union switch
  //todo: support polymorphic setter $[T](x :T)
  //todo: support getter and polymorphic $[T]:T

  type RawOpaque = Vector[Byte]

  //intrinsic xdr type defines
  type void = Unit
  //type defines in .xdr files
  type uint32 = Long
  type uint64 = BigInt
  type int64 = Long
  type SequenceNumber = uint64

  type uint256 = RawOpaque
  type Hash = RawOpaque
  type SignatureHint = RawOpaque
  type Signature = RawOpaque

}

package manual_xdr {

  import shapeless._
  import shapeless.ops.coproduct.{Mapper, Unifier}
  import shapeless.ops.hlist.LeftFolder

object concatOp extends Poly2 {
  implicit  def default[T](implicit xo: Lazy[XDROpaque.Case.Aux[T, RawOpaque]]) =
    at[RawOpaque, T]{ (acc, t) => acc ++ xo.value(t) }
}

object XDROpaque extends Poly1 {

  implicit def caseProduct[T <:Product, L <: HList]
  (implicit gen :Generic.Aux[T, L], xo: Lazy[XDROpaque.Case.Aux[L, RawOpaque]], folder :LeftFolder.Aux[L, RawOpaque, concatOp.type, RawOpaque])
  = at[T]((x:T) => xo.value(gen.to(x)))

  implicit def caseOpaque = at[RawOpaque](identity)

  implicit def  caseHList[L <: HList](implicit folder :LeftFolder.Aux[L, RawOpaque, concatOp.type, RawOpaque]) = at[L]((l :L) => (l.foldLeft(Vector.empty[Byte])(concatOp) :RawOpaque))

  implicit def caseAny[T](implicit st: Lazy[Codec[T]]) = at[T]((x:T) => st.value.encode(x).require.bytes.toIndexedSeq.toVector)

  def from[T](x :T)(implicit xo: Lazy[XDROpaque.Case.Aux[T, RawOpaque]]) = xo.value(x)
}

  object TimeBounds {
    case class Components(minTime :uint64,maxTime :uint64)

    type Struct = TimeBounds
    implicit def codec :Codec[Struct] = (
      XDRUnsignedHyper :: XDRUnsignedHyper
      ).as[Components].xmap(new Struct(_), _.*)

  }
  class TimeBounds(val * :TimeBounds.Components)

  object EnvelopeType
  {
    abstract class Enum(val value :Int)
    case object ENVELOPE_TYPE_SCP extends Enum(1)
    case object ENVELOPE_TYPE_TX extends Enum(2)
    case object ENVELOPE_TYPE_AUTH extends Enum(3)


    implicit def codec :Codec[Enum] = codecs.int32.narrow[Enum](
      {
        case 1 => Attempt.successful(ENVELOPE_TYPE_SCP)
        case 2  => Attempt.successful(ENVELOPE_TYPE_TX)
        case 3 => Attempt.successful(ENVELOPE_TYPE_AUTH)
        case x@_ => Attempt.failure(Err(s"unknow enum value $x"))
      },
      _.value
    )
  }

  object CryptoKeyType {
    abstract class Enum(val value :Int)
    case object KEY_TYPE_ED25519 extends Enum(0)

    def codec :Codec[Enum] = codecs.int32.narrow[Enum](
      {
        case 0 => Attempt.successful(KEY_TYPE_ED25519)
        case x@_ => Attempt.failure(Err(s"unknow enum value $x"))
      },
      _.value
    )
  }

  object PublicKey {
    trait Arm {
      val `type` :CryptoKeyType.Enum
    }
    class  discriminant_KEY_TYPE_ED25519(val arm :uint256) extends Arm{
      val `type` = CryptoKeyType.KEY_TYPE_ED25519
    }

    type Union = discriminant_KEY_TYPE_ED25519 :+: CNil

    implicit def codec :Codec[Union] = codecs.discriminated[Union].by(CryptoKeyType.codec).caseO(
      CryptoKeyType.KEY_TYPE_ED25519)(
      _.select[discriminant_KEY_TYPE_ED25519].map(_.arm))(
      (x) => Coproduct[Union](new discriminant_KEY_TYPE_ED25519(x)))(
        XDRFixedLengthOpaque(32))

    def $KEY_TYPE_ED25519(ed25519 :uint256) = Coproduct[Union](new discriminant_KEY_TYPE_ED25519(ed25519))
  }

  //As typedef of PublicKey
  object AccountID {
    type discriminant_KEY_TYPE_ED25519 = PublicKey.discriminant_KEY_TYPE_ED25519
    type Union = PublicKey.Union
    def codec :Codec[Union] = PublicKey.codec
    def $KEY_TYPE_ED25519(ed25519 :uint256) = Coproduct[Union](new PublicKey.discriminant_KEY_TYPE_ED25519(ed25519))
  }

  object MemoType {
    abstract class Enum(val value :Int)
    case object MEMO_NONE extends Enum(0)
    case object MEMO_TEXT extends Enum(1)
    case object MEMO_ID extends Enum(2)
    case object MEMO_HASH extends Enum(3)
    case object MEMO_RETURN extends Enum(4)

    implicit def codec :Codec[Enum] = codecs.int32.narrow[Enum](
      {
        case 0 => Attempt.successful(MEMO_NONE)
        case 1 => Attempt.successful(MEMO_TEXT)
        case 2  => Attempt.successful(MEMO_ID)
        case 3 => Attempt.successful(MEMO_HASH)
        case 4 => Attempt.successful(MEMO_RETURN)
        case x@_ => Attempt.failure(Err(s"unknow enum value $x"))
      },
      _.value
    )
  };

  object Memo {
    trait Arm {
      val `type` :MemoType.Enum
    }
    class  discriminant_MEMO_NONE() extends Arm  {
      val `type` = MemoType.MEMO_NONE
    }
    class discriminant_MEMO_TEXT(val arm :String) extends Arm {
      val `type` = MemoType.MEMO_TEXT
    }
    class discriminant_MEMO_ID(val arm :uint64) extends Arm {
      val `type` = MemoType.MEMO_ID
    }
    class  discriminant_MEMO_HASH(val arm :Hash) extends Arm {
      val `type` = MemoType.MEMO_HASH
    }
    class discriminant_MEMO_RETURN(val arm :Hash) extends Arm {
      val `type` = MemoType.MEMO_RETURN
    }

    type Union = discriminant_MEMO_NONE :+: discriminant_MEMO_TEXT :+: discriminant_MEMO_ID :+: discriminant_MEMO_HASH :+: discriminant_MEMO_RETURN :+: CNil

    implicit def codec :Codec[Union] = codecs.discriminated[Union].by(MemoType.codec).caseO(
      MemoType.MEMO_NONE)(
      _.select[discriminant_MEMO_NONE].map(_ => ()))(
      (x) => Coproduct[Union](new discriminant_MEMO_NONE()))(
      XDRVoid
    ).caseO(
      MemoType.MEMO_TEXT)(
      _.select[discriminant_MEMO_TEXT].map(_.arm))(
      (x) => Coproduct[Union](new discriminant_MEMO_TEXT(x)))(
      XDRString(Some(28))
    ).caseO(
      MemoType.MEMO_ID)(
      _.select[discriminant_MEMO_ID].map(_.arm))(
      (x) => Coproduct[Union](new discriminant_MEMO_ID(x)))(
      XDRUnsignedHyper
    ).caseO(
      MemoType.MEMO_HASH)(
      _.select[discriminant_MEMO_HASH].map(_.arm))(
      (x) => Coproduct[Union](new discriminant_MEMO_HASH(x)))(
      XDRFixedLengthOpaque(32)
    ).caseO(
      MemoType.MEMO_RETURN)(
      _.select[discriminant_MEMO_RETURN].map(_.arm))(
      (x) => Coproduct[Union](new discriminant_MEMO_RETURN(x)))(
      XDRFixedLengthOpaque(32)
    )

    def $MEMO_NONE() = Coproduct[Union](new discriminant_MEMO_NONE())
    def $MEMO_TEXT(text :String) = Coproduct[Union](new discriminant_MEMO_TEXT(text))
    def $MEMO_ID(id :uint64) = Coproduct[Union](new discriminant_MEMO_ID(id))
    def $MEMO_HASH(hash :Hash) = Coproduct[Union](new discriminant_MEMO_HASH(hash))
    def $MEMO_RETURN(retHash :Hash) = Coproduct[Union](new discriminant_MEMO_RETURN(retHash))
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

    implicit def codec :Codec[Enum] = codecs.int32.narrow[Enum](
      {
        case 0 => Attempt.successful(CREATE_ACCOUNT)
        case 1 => Attempt.successful(PAYMENT)
        case 2  => Attempt.successful(PATH_PAYMENT)
        case 3 => Attempt.successful(MANAGE_OFFER)
        case 4 => Attempt.successful(CREATE_PASSIVE_OFFER)
        case 5 => Attempt.successful(SET_OPTIONS)
        case 6 => Attempt.successful(CHANGE_TRUST)
        case 7  => Attempt.successful(ALLOW_TRUST)
        case 8 => Attempt.successful(ACCOUNT_MERGE)
        case 9 => Attempt.successful(INFLATION)
        case x@_ => Attempt.failure(Err(s"unknow enum value $x"))
      },
      _.value
    )
  }

  object CreateAccountOp {
    case class Components(destination :AccountID.Union, startingBalance :int64)
    type Struct = CreateAccountOp

    implicit def codec :Codec[Struct] = (
      AccountID.codec :: XDRHyper
      ).as[Components].xmap(new Struct(_), _.*)
  }
  class CreateAccountOp(val * :CreateAccountOp.Components)

  //case class PaymentOp(destination :AccountID, asset :Asset, amount :int64) //todo Asset
  case class PaymentOp(destination :AccountID.Union, amount :int64)

  object Operation {
    object Union_body {
      trait Arm {
        val `type` :OperationType.Enum
      }
      class discriminant_CREATE_ACCOUNT(val arm :CreateAccountOp) extends Arm {
        val `type` = OperationType.CREATE_ACCOUNT
      }
      class discriminant_PAYMENT(val arm :PaymentOp) extends Arm {
        val `type` = OperationType.PAYMENT
      }
      class discriminant_ACCOUNT_MERGE(val arm :AccountID.Union) extends Arm {
        val `type` = OperationType.ACCOUNT_MERGE
      }
      class discriminant_INFLATION() extends Arm {
        val `type` = OperationType.INFLATION
      }

      type Union = discriminant_CREATE_ACCOUNT :+: discriminant_PAYMENT :+: discriminant_ACCOUNT_MERGE :+: discriminant_INFLATION :+: CNil

      implicit def codec :Codec[Union] = codecs.discriminated[Union].by(OperationType.codec).caseO(
        OperationType.INFLATION)(
        _.select[discriminant_INFLATION].map(x => ()))(
        (x) => Coproduct[Union](new discriminant_INFLATION()))(
        XDRVoid
      ).caseO(
        OperationType.CREATE_ACCOUNT)(
        _.select[discriminant_CREATE_ACCOUNT].map(_.arm))(
        (x) => Coproduct[Union](new discriminant_CREATE_ACCOUNT(x)))(
        CreateAccountOp.codec
      ) .caseO(
        OperationType.ACCOUNT_MERGE)(
        _.select[discriminant_ACCOUNT_MERGE].map(_.arm))(
        (x) => Coproduct[Union](new discriminant_ACCOUNT_MERGE(x)))(
        AccountID.codec
      )//TODO

      def $CREATE_ACCOUNT(createAccountOp :CreateAccountOp) = Coproduct[Union](new discriminant_CREATE_ACCOUNT(createAccountOp))
      def $PAYMENT(paymentOp :PaymentOp) = Coproduct[Union](new discriminant_PAYMENT(paymentOp))
      def $ACCOUNT_MERGE(destination :AccountID.Union) = Coproduct[Union](new discriminant_ACCOUNT_MERGE(destination))
      def $INFLATION() = Coproduct[Union](new discriminant_INFLATION())
    }

    case class Components(sourceAccount :Option[AccountID.Union], body :Operation.Union_body.Union)

    type Struct = Operation

    implicit def codec :Codec[Struct] = (
      XDROptional(AccountID.codec) :: Operation.Union_body.codec
      ).as[Components].xmap(new Struct(_), _.*)

    def apply(body :Operation.Union_body.Union) = new Operation(Components(None, body))

  }

  class Operation(val * :Operation.Components) {
    def $sourceAccount(sourceAccount :AccountID.Union) = new Operation(*.copy(sourceAccount = Some(sourceAccount)))
  }

  object Transaction {
    object Union_ext {
      trait Arm {
        val v :Int
      }
      class discriminant_0() extends Arm {
        val v = 0
      }
      type Union = discriminant_0 :+: CNil

      implicit def codec :Codec[Union] = codecs.discriminated[Union].by(XDRInteger).caseO(
        0)(
        _.select[discriminant_0].map(x => ()))(
        (x) => Coproduct[Union](new discriminant_0()))(
        XDRVoid
      )

      def $0() = Coproduct[Union](new discriminant_0())

    }
    case class Components(sourceAccount :AccountID.Union, fee :uint32, seqNum :SequenceNumber, timeBounds :Option[TimeBounds], memo :Memo.Union,  operations :Vector[Operation], ext :Transaction.Union_ext.Union)

    def apply(sourceAccount :AccountID.Union, fee :uint32, seqNum :SequenceNumber, memo :Memo.Union, ext :Transaction.Union_ext.Union)
    = new Transaction(Components(sourceAccount, fee , seqNum, None, memo, Vector.empty[Operation], ext))

    type Struct = Transaction

    implicit def codec :Codec[Struct] = (
      AccountID.codec :: XDRUnsignedInteger :: XDRUnsignedHyper:: XDROptional(TimeBounds.codec)  :: Memo.codec :: XDRVariableLengthArray(Some(100), Operation.codec)
        :: Transaction.Union_ext.codec).as[Components].xmap(new Struct(_), _.*)
  }

  class Transaction(val * :Transaction.Components) {
    def $timeBounds(x :TimeBounds) = new Transaction(*.copy(timeBounds = Some(x)))
    def $operations(x :Operation*) = new Transaction(*.copy(operations = x.toVector))
  }

  object DecoratedSignature {
    case class Components(hint :SignatureHint, signature :Signature)

    type Struct = DecoratedSignature

    implicit def codec :Codec[Struct] = (
      XDRFixedLengthOpaque(4) :: XDRVariableLengthOpaque(Some(64))
      ).as[Components].xmap(new Struct(_), _.*)

    def apply(hint :SignatureHint, signature :Signature) = new DecoratedSignature(Components(hint, signature))
  }
  class DecoratedSignature(val * :DecoratedSignature.Components)

  object TransactionEnvelope {
    case class Components(tx :Transaction, signatures :Vector[DecoratedSignature])
    def apply(tx :Transaction) = new TransactionEnvelope(Components(tx, Vector.empty[DecoratedSignature]))

    type Struct = TransactionEnvelope

    implicit def codec :Codec[Struct] = (
      Transaction.codec ~ XDRVariableLengthArray(Some(20), DecoratedSignature.codec)
      ).widenOpt((x) => new Struct(Components.tupled(x)), (x) => Components.unapply(x.*))
  }

  class TransactionEnvelope(val * :TransactionEnvelope.Components) {
    def $signatures(signatures :DecoratedSignature*) = new TransactionEnvelope(*.copy(signatures = signatures.toVector))
  }

}

package stellar_xdr {
import manual_xdr._

object ops {

  implicit def accountid2xdr(acct :org.strllar.stellarbase.StrAccountID) :AccountID.Union = AccountID.$KEY_TYPE_ED25519(acct.rawbytes.toVector)

  implicit def AccountIDOPs(acct :AccountID.Union) = new {
    def hint :SignatureHint = {
      acct.select[AccountID.discriminant_KEY_TYPE_ED25519].get.arm.takeRight(4).toVector
    }
  }

  implicit def TransactionOPs(tx :Transaction) = new {
    def hashid(implicit network :Network) = { //:Hash256
      val md256 = new Sha256()
      md256.update(XDROpaque.from(network.networkId, EnvelopeType.ENVELOPE_TYPE_TX :EnvelopeType.Enum, tx))
      md256.digest
    }
  }

  implicit def  AccountOP(seed :StrSeed) = new {
    def signTx(tx :Transaction)(implicit network :Network) = {
      val acct :AccountID.Union = seed.accountid
      DecoratedSignature(acct.hint, seed.sign(tx.hashid).toVector)
    }
  }

}

}