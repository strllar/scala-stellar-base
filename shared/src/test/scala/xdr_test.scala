//package mytests
//
//import com.inthenow.zcheck.SpecLite
//import xdr_generated.Memo.discriminant_MEMO_TEXT
//import xdr_generated.Operation.anon_body_type.{discriminant_ACCOUNT_MERGE, discriminant_CREATE_PASSIVE_OFFER}
//import xdr_generated.Transaction.anon_ext_type.discriminant_0
//
//object XDRDSLSpec extends SpecLite {
//  "Nothing" should {
//    "be not wrong" in {
//      import org.strllar.stellarbase.{Sha256, StrKey, Network}
//      import org.strllar.stellarbase.Networks.XLMTestnet
//      import org.strllar.stellarbase.manual_xdr._
//      import org.strllar.stellarbase.stellar_xdr.ops._
//
//      import scodec.bits._
//
//      val dashang = implicitly[Network].keyFactory.parseAccountID("GAKIEJNSVB44WDEVNDY6RGRVOXWHZWYM7V3UULOZBVISONXM76OYHRGU").get
//      val master = StrKey.master
//
//      val tx =
//        Transaction(master.accountid, 100, 1, Memo.$MEMO_TEXT("hello world"), Transaction.Union_ext.$0())
//          .$operations(Operation(Operation.Union_body.$ACCOUNT_MERGE(dashang)))
//
//      ByteVector(XDROpaque.from(tx)) must_== hex"0000000062fc1d0bd091b2b61c0dd656346b2a68d7d347c6f2c2c8ee6d04470256fc05f700000064000000000000000100000000000000010000000b68656c6c6f20776f726c640000000001000000000000000800000000148225b2a879cb0c9568f1e89a3575ec7cdb0cfd774a2dd90d512736ecff9d8300000000"
//
//      val hash = tx.hashid
//
//      ByteVector(hash) must_== hex"c4b156817fb21ac7b5da102b2530ec870d0b03bbb2c840f205ce7eaa6e22d1a2"
//
//      val ds = master.signTx(tx)
//      ByteVector(XDROpaque.from(ds)) must_== hex"56fc05f70000004079d4e247150c0e2b4868f414f2474763b336c7ae74aa58220c4d2dd4727910ed9519d8d6505084a3224c80bbc2c3a8e2eb702c6b1a5b895fff68845169bd3e00"
//
//
//      val txenv = TransactionEnvelope(tx).$signatures(ds)
//
//      ByteVector(XDROpaque.from(txenv)) must_== hex"0000000062fc1d0bd091b2b61c0dd656346b2a68d7d347c6f2c2c8ee6d04470256fc05f700000064000000000000000100000000000000010000000b68656c6c6f20776f726c640000000001000000000000000800000000148225b2a879cb0c9568f1e89a3575ec7cdb0cfd774a2dd90d512736ecff9d83000000000000000156fc05f70000004079d4e247150c0e2b4868f414f2474763b336c7ae74aa58220c4d2dd4727910ed9519d8d6505084a3224c80bbc2c3a8e2eb702c6b1a5b895fff68845169bd3e00"
//    }
//  }
//}
//
//object XDRDSLAltSpec extends SpecLite {
//  "Nothing" should {
//    "be not wrong" in {
//      import org.strllar.stellarbase.{Sha256, StrKey, Network}
//      import org.strllar.stellarbase.Networks.XLMTestnet
//      import xdr_generated._
//      import shapeless.Coproduct
//
//      import scodec.bits._
//
//      val dashang = implicitly[Network].keyFactory.parseAccountID("GAKIEJNSVB44WDEVNDY6RGRVOXWHZWYM7V3UULOZBVISONXM76OYHRGU").get
//      val master = StrKey.master
//
////      val tx =
////        Transaction(master.accountid, 100, 1, Memo.$MEMO_TEXT("hello world"), Transaction.Union_ext.$0())
////          .$operations(Operation(Operation.Union_body.$ACCOUNT_MERGE(dashang)))
//      val masterpub = Coproduct[PublicKey.Union](new PublicKey.discriminant_KEY_TYPE_ED25519(master.accountid.rawbytes.toVector))
//      val dashangpub = Coproduct[PublicKey.Union](new PublicKey.discriminant_KEY_TYPE_ED25519(dashang.rawbytes.toVector))
//      val tx = new Transaction(Transaction.Components(masterpub,
//        100, 1, None, Coproduct[Memo.Union](new discriminant_MEMO_TEXT("hello world")),
//        Vector(new Operation(Operation.Components(None, Coproduct[Operation.anon_body_type.Union](new discriminant_ACCOUNT_MERGE(dashangpub))))),
//        Coproduct[Transaction.anon_ext_type.Union](new discriminant_0())
//      ))
//
//      Transaction.codec.encode(tx).require.bytes must_== hex"0000000062fc1d0bd091b2b61c0dd656346b2a68d7d347c6f2c2c8ee6d04470256fc05f700000064000000000000000100000000000000010000000b68656c6c6f20776f726c640000000001000000000000000800000000148225b2a879cb0c9568f1e89a3575ec7cdb0cfd774a2dd90d512736ecff9d8300000000"
////
////      val hash = tx.hashid
////
////      ByteVector(hash) must_== hex"c4b156817fb21ac7b5da102b2530ec870d0b03bbb2c840f205ce7eaa6e22d1a2"
////
////      val ds = master.signTx(tx)
////      ByteVector(XDROpaque.from(ds)) must_== hex"56fc05f70000004079d4e247150c0e2b4868f414f2474763b336c7ae74aa58220c4d2dd4727910ed9519d8d6505084a3224c80bbc2c3a8e2eb702c6b1a5b895fff68845169bd3e00"
////
////
////      val txenv = TransactionEnvelope(tx).$signatures(ds)
////
////      ByteVector(XDROpaque.from(txenv)) must_== hex"0000000062fc1d0bd091b2b61c0dd656346b2a68d7d347c6f2c2c8ee6d04470256fc05f700000064000000000000000100000000000000010000000b68656c6c6f20776f726c640000000001000000000000000800000000148225b2a879cb0c9568f1e89a3575ec7cdb0cfd774a2dd90d512736ecff9d83000000000000000156fc05f70000004079d4e247150c0e2b4868f414f2474763b336c7ae74aa58220c4d2dd4727910ed9519d8d6505084a3224c80bbc2c3a8e2eb702c6b1a5b895fff68845169bd3e00"
//    }
//  }
//}
