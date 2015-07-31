package mytests

import com.inthenow.zcheck.{SpecLite}

import org.strllar.stellarbase.StrSeed

object StrKeySpec extends SpecLite {
  "master StrKey should" should {
    "encode master" in {
      val master = StrSeed("allmylifemyhearthasbeensearching".getBytes)
      master.toString() must_== "SBQWY3DNPFWGSZTFNV4WQZLBOJ2GQYLTMJSWK3TTMVQXEY3INFXGO52X"
      master.address.toString() must_== "GCEZWKCA5VLDNRLN3RPRJMRZOX3Z6G5CHCGSNFHEYVXM3XOJMDS674JZ"
    }
  }
}