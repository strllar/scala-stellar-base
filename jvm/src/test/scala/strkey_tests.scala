package mytests

import com.inthenow.zcheck.{SpecLite}

import org.strllar.stellarbase.{StrSeed, StrAddress}

import scala.util.Try

object TestKeys {
  val master = StrSeed("allmylifemyhearthasbeensearching".getBytes)
  val adam = StrSeed("tian wang gai di hu?????????????".getBytes)
  val eve = StrSeed("bao ta zhen he yao!!!!!!!!!!!!!!".getBytes)
}

object StrSeedSpec extends SpecLite {
  import TestKeys._
  "StrSeed should" should {
    "generate correct string" in {
      master.toString() must_== "SBQWY3DNPFWGSZTFNV4WQZLBOJ2GQYLTMJSWK3TTMVQXEY3INFXGO52X"
      adam.toString() must_== "SB2GSYLOEB3WC3THEBTWC2JAMRUSA2DVH47T6PZ7H47T6PZ7H47T6432"
      eve.toString() must_== "SBRGC3ZAORQSA6TIMVXCA2DFEB4WC3ZBEEQSCIJBEEQSCIJBEEQSC5LX"
    }
    "genderate correct address" in {
      master.address.toString() must_== "GCEZWKCA5VLDNRLN3RPRJMRZOX3Z6G5CHCGSNFHEYVXM3XOJMDS674JZ"
      adam.address.toString() must_== "GDPE6EL3TCRSB5PERFORHRM4FWHNPAQ4YXDYKMQZ2IISJKR4UYZNX4US"
      eve.address.toString() must_== "GBCJ6N6TUBLHKHSA5EDOTAWCQYEHBX6T2JX7FKGSQRGONAL7Z6TROSTT"
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
