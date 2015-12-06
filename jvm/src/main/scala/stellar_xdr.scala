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

  //converters
  implicit def accountid2xdr(acct :org.strllar.stellarbase.StrAccountID) :AccountID = PublicKey.$KEY_TYPE_ED25519(uint256(Vector[Byte]()))

  //intrinsic xdr type defines
  type void = Unit
  //type defines in .xdr files
  type uint32 = Int
  type uint64 = Long
  type int64 = Long
  type SequenceNumber = uint64
  type AccountID = PublicKey.Union

}

package manual_xdr {

import akka.http.scaladsl.marshalling.Marshalling.Opaque
import shapeless._, shapeless.ops.hlist.LeftFolder, shapeless.ops.coproduct.Mapper, ops.coproduct.Unifier

  object concatOp extends Poly2 {
    implicit  def default[T](implicit xo: Lazy[XDROpaque.Case.Aux[T, RawOpaque]]) =
      at[RawOpaque, T]{ (acc, t) => acc ++ xo.value(t) }
  }
  object XDROpaque extends Poly1 {

    //intrinsic XDR rules

    //implicit def caseInt32 = at[uint32](x => Vector.empty[Byte])
    implicit def caseInt = at[Int](x => Vector[Byte](((x >>> 24) & 0xff).toByte, ((x >>> 16) & 0xff).toByte, ((x >>> 8) & 0xff).toByte, (x & 0xff).toByte))

    implicit def caseInt64 = at[uint64](x => Vector.empty[Byte])

    implicit def caseVector[T] = at[Vector[T]](x => Vector.empty[Byte])

    implicit def caseString = at[String](x => Vector.empty[Byte])

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
  case class SignatureHint(opaqueM64 :RawOpaque)
  case class Signature(opaqueN4 :RawOpaque)
  case class TimeBounds(minTime :uint64,maxTime :uint64)

  object CryptoKeyType {
    abstract class Enum(val value :Int) {
      def toUnsigned :Long = value & 0xffffffff
    }
    case object KEY_TYPE_ED25519 extends Enum(0)
  }

  object PublicKey {
    trait Arm extends XDRStagedItem {
      val `type` :CryptoKeyType.Enum
    }
    class  arm_KEY_TYPE_ED25519(val ed25519 :uint256) extends Arm{
      val `type` = CryptoKeyType.KEY_TYPE_ED25519
      override  def toOpaque = XDROpaque.from(`type`) ++ XDROpaque.from(ed25519)
    }

    type Union = arm_KEY_TYPE_ED25519 :+: CNil

    def $KEY_TYPE_ED25519(ed25519 :uint256) = Coproduct[Union](new arm_KEY_TYPE_ED25519(ed25519))
  }

  object MemoType {
    abstract class Enum(val value :Int) {
      def toUnsigned :Long = value & 0xffffffff
    }
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
      override  def toOpaque = XDROpaque.from(`type`)
    }
    class arm_MEMO_TEXT(val text :String) extends Arm {
      val `type` = MemoType.MEMO_TEXT
      override  def toOpaque = XDROpaque.from(`type` :: text :: HNil)
    }
    class arm_MEMO_ID(val id :uint64) extends Arm {
      val `type` = MemoType.MEMO_ID
      override  def toOpaque = XDROpaque.from(`type` :: id :: HNil)
    }
    class  arm_MEMO_HASH(val hash :Hash) extends Arm {
      val `type` = MemoType.MEMO_HASH
      override  def toOpaque = XDROpaque.from(`type` :: hash :: HNil)
    }
    class arm_MEMO_RETURN(val retHash :Hash) extends Arm {
      val `type` = MemoType.MEMO_RETURN
      override  def toOpaque = XDROpaque.from(`type` :: retHash :: HNil)
    }

    type Union = arm_MEMO_NONE :+: arm_MEMO_TEXT :+: arm_MEMO_ID :+: arm_MEMO_HASH :+: arm_MEMO_RETURN :+: CNil

    def $MEMO_NONE() = Coproduct[Union](new arm_MEMO_NONE())
    def $MEMO_TEXT(text :String) = Coproduct[Union](new arm_MEMO_TEXT(text))
    def $MEMO_ID(id :uint64) = Coproduct[Union](new arm_MEMO_ID(id))
    def $MEMO_HASH(hash :Hash) = Coproduct[Union](new arm_MEMO_HASH(hash))
    def $MEMO_RETURN(retHash :Hash) = Coproduct[Union](new arm_MEMO_RETURN(retHash))
  }

  object OperationType {
    abstract class Enum(val value :Int) {
      def toUnsigned :Long = value & 0xffffffff
    }
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

  case class CreateAccountOp(destination :AccountID, startingBalance :int64)

  //case class PaymentOp(destination :AccountID, asset :Asset, amount :int64) //todo Asset
  case class PaymentOp(destination :AccountID, amount :int64)

  object Operation {
    object Union_body {
      trait Arm {
        val `type` :OperationType.Enum
      }
      class arm_CREATE_ACCOUNT(val createAccountOp :CreateAccountOp) extends Arm {
        val `type` = OperationType.CREATE_ACCOUNT
      }
      class arm_PAYMENT(val paymentOp :PaymentOp) extends Arm {
        val `type` = OperationType.PAYMENT
      }
      class arm_ACCOUNT_MERGE(val destination :AccountID) extends Arm {
        val `type` = OperationType.ACCOUNT_MERGE
      }
      class arm_INFLATION() extends Arm {
        val `type` = OperationType.INFLATION
      }

      type Union = arm_CREATE_ACCOUNT :+: arm_PAYMENT :+: arm_ACCOUNT_MERGE :+: arm_INFLATION :+: CNil

      def $CREATE_ACCOUNT(createAccountOp :CreateAccountOp) = Coproduct[Union](new arm_CREATE_ACCOUNT(createAccountOp))
      def $PAYMENT(paymentOp :PaymentOp) = Coproduct[Union](new arm_PAYMENT(paymentOp))
      def $ACCOUNT_MERGE(destination :AccountID) = Coproduct[Union](new arm_ACCOUNT_MERGE(destination))
      def $INFLATION() = Coproduct[Union](new arm_INFLATION())
    }

    def apply(body :Operation.Union_body.Union) = new Operation(body)
  }

  class Operation(val body :Operation.Union_body.Union) {
    def $sourceAccount(sourceAccount :AccountID) = this
  }

  object Transaction {
    object Union_ext {
      abstract class Enum(val value :Int) {
        def toUnsigned :Long = value & 0xffffffff
      }
      case object _0 extends Enum(0)

      trait Arm extends XDRStagedItem {
        val v :Enum
      }
      class arm_0() extends Arm {
        val v = _0
        override def toOpaque = Vector.empty[Byte]
      }
      type Union = arm_0 :+: CNil

      def $0() = Coproduct[Union](new arm_0())

    }
    case class Struct(sourceAccount :AccountID, fee :uint32, seqNum :SequenceNumber, memo :Memo.Union, ext :Transaction.Union_ext.Union, timeBounds :Option[TimeBounds], operations :Vector[Operation] )

    def apply(sourceAccount :AccountID, fee :uint32, seqNum :SequenceNumber, memo :Memo.Union, ext :Transaction.Union_ext.Union)
    = new Transaction(Struct(sourceAccount, fee , seqNum, memo, ext, None, Vector.empty[Operation]))

    implicit def toOpaque(* :Transaction) =  new XDRStagedItem {
      import *.*._
      override def toOpaque = XDROpaque(sourceAccount :: fee :: seqNum :: timeBounds :: memo :: operations :: ext :: HNil)
    }
  }

  class Transaction(val * :Transaction.Struct) {
    def $timeBounds(x :TimeBounds) = new Transaction(*.copy(timeBounds = Some(x)))
    def $operations(x :Operation*) = new Transaction(*.copy(operations = x.toVector))
  }

  case class DecoratedSignature(hint :SignatureHint, signature :Signature)

  object TransactionEnvelope {
    def apply(tx :Transaction) = new TransactionEnvelope(tx)
  }

  class TransactionEnvelope(val tx :Transaction) {
    def $signatures(signaturese :DecoratedSignature*) = this
  }

}

package stellar_xdr {


//    struct Transaction
//    {
//    // account used to run the transaction
//    AccountID sourceAccount;
//
//    // the fee the sourceAccount will pay
//    uint32 fee;
//
//    // sequence number to consume in the account
//    SequenceNumber seqNum;
//
//    // validity range (inclusive) for the last ledger close time
//    TimeBounds* timeBounds;
//
//    Memo memo;
//
//    Operation operations<100>;
//
//    // reserved for future use
//    union switch (int v)
//    {
//    case 0:
//    void;
//    }
//    ext;
//    };

  //import org.strllar.stellarbase.compiled_xdr._


  object Test {

    //val trans = xdr_generator.XDRGen[Transaction]

    def test(): Unit = {
      //trans.toXDR()

//      class aSampleStruct {
//        val f1:Int
//        val f2:Option[Array[Byte]]
//        val f3 = unionSwitch[EnumType](armType[SwitchEntry1].as "name1",
//        armtype[SwitchEntry2].as"name2",
//        armtype[SwitchEntry3].as"name3") {
//          case 1 => "name1";
//          case 2 => "name2";
//          case 3 => "name3";
//        }
//      }
    }
  }
}
