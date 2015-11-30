package mytests

import com.inthenow.zcheck.SpecLite
import org.strllar.stellarbase.{StrAddress, StrSeed}

object TestKeys {
  val master = StrSeed("allmylifemyhearthasbeensearching".getBytes)
  val adam = StrSeed("tian wang gai di hu?????????????".getBytes)
  val eve = StrSeed("bao ta zhen he yao!!!!!!!!!!!!!!".getBytes)

  val tao = StrSeed(Array(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31).map(_.toByte))
  val yin = StrSeed(Array(2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50,52,54,56,58,60,62,64).map(_.toByte))
  val yang = StrSeed(Array(-1,-3,-5,-7,-9,-11,-13,-15,-17,-19,-21,-23,-25,-27,-29,-31,-33,-35,-37,-39,-41,-43,-45,-47,-49,-51,-53,-55,-57,-59,-61,-63).map(_.toByte))
}

object StrSeedSpec extends SpecLite {
  import TestKeys._
  "StrSeed should" should {
    "generate correct string" in {
      master.toString() must_== "SBQWY3DNPFWGSZTFNV4WQZLBOJ2GQYLTMJSWK3TTMVQXEY3INFXGO52X"
      adam.toString() must_== "SB2GSYLOEB3WC3THEBTWC2JAMRUSA2DVH47T6PZ7H47T6PZ7H47T6432"
      eve.toString() must_== "SBRGC3ZAORQSA6TIMVXCA2DFEB4WC3ZBEEQSCIJBEEQSCIJBEEQSC5LX"

      tao.toString() must_== "SAAACAQDAQCQMBYIBEFAWDANBYHRAEISCMKBKFQXDAMRUGY4DUPB6NKI"
      yin.toString() must_== "SABAIBQIBIGA4EASCQLBQGQ4DYQCEJBGFAVCYLRQGI2DMOB2HQ7EA4H2"
      yang.toString() must_== "SD77367Z6727H4PP5XV6TZ7F4PQ57XO33HL5LU6RZ7G4XSOHYXB4DINL"
    }
    "genderate correct address" in {
      master.address.toString() must_== "GCEZWKCA5VLDNRLN3RPRJMRZOX3Z6G5CHCGSNFHEYVXM3XOJMDS674JZ"
      adam.address.toString() must_== "GDPE6EL3TCRSB5PERFORHRM4FWHNPAQ4YXDYKMQZ2IISJKR4UYZNX4US"
      eve.address.toString() must_== "GBCJ6N6TUBLHKHSA5EDOTAWCQYEHBX6T2JX7FKGSQRGONAL7Z6TROSTT"

      tao.address.toString() must_== "GAB2CB576PHBBPQ5ODORRZ2LYCMWPZGWGCN2KDK7DXOIMZASKUY3QZ6Q"
      yin.address.toString() must_== "GC3BWHRPM3SQFRUCM3JY2TTHLETTXKCQNE2BJYFLJXRCEFIMAVQB552G"
      yang.address.toString() must_== "GDHEFJQIKJ6JTTOOBTQHTXEXGQJHUK45JL5LYSR37O2SZLYLTUWR755D"
    }
    "parse correct string" in {
      val master2 = StrSeed.parse("SBQWY3DNPFWGSZTFNV4WQZLBOJ2GQYLTMJSWK3TTMVQXEY3INFXGO52X")
      master2.get.address.toString() must_== "GCEZWKCA5VLDNRLN3RPRJMRZOX3Z6G5CHCGSNFHEYVXM3XOJMDS674JZ"
    }
    "throw when parse wrong string" in {
      val shorten_master = StrSeed.parse("SBQWY3DNPF")
      shorten_master.get.mustThrowA[Exception]
      val typo_master = StrSeed.parse("SBQWY3DNPFWGSZTFNV4WQZLBOJ2GQYLTMJSWK3TTMVQXEY3INFXGO52Y")
      typo_master.get.mustThrowA[Exception]
    }
    //TODO
    "correct misreading charater by base32 constraint" in {
      ////replace O=>0, I=>1, etc
      //val ret = StrSeed.parse("SBQWY3DNPFWGSZTFNV4WQZLBOJ2GQYLTMJSWK3TTMVQXEY3INFXGO52X", true)
    }

  }
}

object StrAddressSpec extends SpecLite {
  val test_address = StrAddress(1 to 32 map(_.toByte) toArray)
  "genderate correct address" in {
    test_address.toString()  must_== "GAAQEAYEAUDAOCAJBIFQYDIOB4IBCEQTCQKRMFYYDENBWHA5DYPSABOV"
  }
  "parse correct string" in {
    val test_address2 = StrAddress.parse("GAAQEAYEAUDAOCAJBIFQYDIOB4IBCEQTCQKRMFYYDENBWHA5DYPSABOV")
    test_address2.get.toString() must_== "GAAQEAYEAUDAOCAJBIFQYDIOB4IBCEQTCQKRMFYYDENBWHA5DYPSABOV"
  }
}
