package mytests

import com.inthenow.zcheck.{SpecLite}
import org.strllar.stellarbase.{BaseN, Hex, Lit}

object BaseNSpec extends SpecLite {

//  "Hex should" should {
//    "be constructed from legal hex string" in {
//      Hex("a dead beef bedded a defaced babe")
//      Hex("C0FFEEBABE")
//    }
//    "be constructed from 0xx tuple" in {
//      Hex(0xde, 0xca, 0xde)
//      Hex(0xCA, 0xFE, 0xBA, 0xBE)
//    }
//    "throw from illegal hex string" in {
//      Hex("decades")
//      Hex("C0FFEEBABE!")
//    }
//  }

  "Base32 should" should {
    val b32 = BaseN.base32
    "encode" in {
      Lit("ME======").asBase32 must_== Lit("a")
      Lit("a").toBase32 must_==  Lit("ME======").asBase32
      b32.encode("a".getBytes) must_== "ME======"
      b32.encode("be".getBytes) must_== "MJSQ===="
      b32.encode("bee".getBytes) must_== "MJSWK==="
      b32.encode("beer".getBytes) must_== "MJSWK4Q="
      b32.encode("beers".getBytes) must_== "MJSWK4TT"
      b32.encode("beers 1".getBytes) must_== "MJSWK4TTEAYQ===="
      b32.encode("shockingly dismissed".getBytes) must_== "ONUG6Y3LNFXGO3DZEBSGS43NNFZXGZLE"
    }
    "decode" in {
      check(b32.decode("ME======") sameElements "a")
      check(b32.decode("MJSQ====") sameElements "be")
      check(b32.decode("ONXW4===") sameElements "son")
      check(b32.decode("MJSWK===") sameElements "bee")
      check(b32.decode("MJSWK4Q=") sameElements "beer")
      check(b32.decode("MJSWK4TT") sameElements "beers")
      check(b32.decode("MJSWK4TTN5XA====") sameElements "beerson")
      check(b32.decode("MJSWK4TTEAYQ====") sameElements "beers 1")
      check(b32.decode("ONUG6Y3LNFXGO3DZEBSGS43NNFZXGZLE") sameElements "shockingly dismissed")
    }

    def BASE64(s:String) = BaseN.base64.encode(s.getBytes)
    def BASE32(s:String) = BaseN.base32.encode(s.getBytes)
    def BASE32HEX(s:String) = BaseN.base32hex.encode(s.getBytes)
    def BASE16(s:String) = BaseN.base16.encode(s.getBytes)
    "pass Test Vectors in RFC 4648" in {
      BASE64("") must_== ""
      BASE64("f") must_== "Zg=="
      BASE64("fo") must_== "Zm8="
      BASE64("foo") must_== "Zm9v"
      BASE64("foob") must_== "Zm9vYg=="
      BASE64("fooba") must_== "Zm9vYmE="
      BASE64("foobar") must_== "Zm9vYmFy"
      BASE32("") must_== ""
      BASE32("f") must_== "MY======"
      BASE32("fo") must_== "MZXQ===="
      BASE32("foo") must_== "MZXW6==="
      BASE32("foob") must_== "MZXW6YQ="
      BASE32("fooba") must_== "MZXW6YTB"
      BASE32("foobar") must_== "MZXW6YTBOI======"
      BASE32HEX("") must_== ""
      BASE32HEX("f") must_== "CO======"
      BASE32HEX("fo") must_== "CPNG===="
      BASE32HEX("foo") must_== "CPNMU==="
      BASE32HEX("foob") must_== "CPNMUOG="
      BASE32HEX("fooba") must_== "CPNMUOJ1"
      BASE32HEX("foobar") must_== "CPNMUOJ1E8======"
      BASE16("") must_== ""
      BASE16("f") must_== "66"
      BASE16("fo") must_== "666F"
      BASE16("foo") must_== "666F6F"
      BASE16("foob") must_== "666F6F62"
      BASE16("fooba") must_== "666F6F6261"
      BASE16("foobar") must_== "666F6F626172"
    }
    "be binary safe" in {
      b32.decode(
        b32.encode(Array(0x00, 0xff, 0x88).map(_.toByte))
      ).deep  must_==  Hex("00ff88").bytes.deep

      b32.encode(Hex("f61e1f998d69151de8334dbe753ab17ae831c13849a6aecd95d0a4e5dc25").bytes).must_==("6YPB7GMNNEKR32BTJW7HKOVRPLUDDQJYJGTK5TMV2CSOLXBF")

      b32.decode("6YPB7GMNNEKR32BTJW7HKOVRPLUDDQJYJGTK5TMV2CSOLXBF").deep must_== Hex("f61e1f998d69151de8334dbe753ab17ae831c13849a6aecd95d0a4e5dc25").bytes.deep
    }
  }
}