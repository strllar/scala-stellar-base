package mytests

import org.scalatest._

import org.strllar.stellarbase.{StrAccountID, StrSeed, StrKey}

object TestKeys {
  import org.strllar.stellarbase.Networks.XLMLive

  val master = StrKey.legacyMaster()
  val adam = new StrSeed("tian wang gai di hu?????????????".getBytes)
  val eve = new StrSeed("bao ta zhen he yao!!!!!!!!!!!!!!".getBytes)

  val tao = new StrSeed(Array(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31).map(_.toByte))
  val yin = new StrSeed(Array(2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50,52,54,56,58,60,62,64).map(_.toByte))
  val yang = new StrSeed(Array(-1,-3,-5,-7,-9,-11,-13,-15,-17,-19,-21,-23,-25,-27,-29,-31,-33,-35,-37,-39,-41,-43,-45,-47,-49,-51,-53,-55,-57,-59,-61,-63).map(_.toByte))
}

object StrSeedSpec extends FlatSpec with Matchers {
  import TestKeys._
  "Root accounts in various networks" should "pass all" in {
    it should "be correct" in {
      StrKey.master()(org.strllar.stellarbase.Networks.XLMLive).toString() shouldEqual "SB5MGOMXKRHDC5OSM26QEJBZWIWNWFSQRQARMPZG4XFSUPQQIWUXTVO6"
      StrKey.master()(org.strllar.stellarbase.Networks.XLMLive).accountid.toString() shouldEqual "GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7"

      StrKey.master()(org.strllar.stellarbase.Networks.XLMTestnet).toString() shouldEqual "SDHOAMBNLGCE2MV5ZKIVZAQD3VCLGP53P3OBSBI6UN5L5XZI5TKHFQL4"
      StrKey.master()(org.strllar.stellarbase.Networks.XLMTestnet).accountid.toString() shouldEqual "GBRPYHIL2CI3FNQ4BXLFMNDLFJUNPU2HY3ZMFSHONUCEOASW7QC7OX2H"

      StrKey.master()(org.strllar.stellarbase.Networks.KLMLive).toString() shouldEqual "XZISUMAUUY622JJW4S6POVMWDAY7DSP4QAOEQGTJJLICWC3TCM734VWL"
      StrKey.master()(org.strllar.stellarbase.Networks.KLMLive).accountid.toString() shouldEqual "NBIFBSRNGRWNFBOL5KRH3RTNDZEHQSMLCDXYQ2INUE4IN7H6NO6UPPAQ"
    }
  }
  "StrSeed should" should "pass all" in {
    import org.strllar.stellarbase.Networks.XLMLive

    it should "generate correct string" in {
      master.toString() shouldEqual "SBQWY3DNPFWGSZTFNV4WQZLBOJ2GQYLTMJSWK3TTMVQXEY3INFXGO52X"
      adam.toString() shouldEqual "SB2GSYLOEB3WC3THEBTWC2JAMRUSA2DVH47T6PZ7H47T6PZ7H47T6432"
      eve.toString() shouldEqual "SBRGC3ZAORQSA6TIMVXCA2DFEB4WC3ZBEEQSCIJBEEQSCIJBEEQSC5LX"

      tao.toString() shouldEqual "SAAACAQDAQCQMBYIBEFAWDANBYHRAEISCMKBKFQXDAMRUGY4DUPB6NKI"
      yin.toString() shouldEqual "SABAIBQIBIGA4EASCQLBQGQ4DYQCEJBGFAVCYLRQGI2DMOB2HQ7EA4H2"
      yang.toString() shouldEqual "SD77367Z6727H4PP5XV6TZ7F4PQ57XO33HL5LU6RZ7G4XSOHYXB4DINL"
    }
    it should "genderate correct address" in {
      master.accountid.toString() shouldEqual "GCEZWKCA5VLDNRLN3RPRJMRZOX3Z6G5CHCGSNFHEYVXM3XOJMDS674JZ"
      adam.accountid.toString() shouldEqual "GDPE6EL3TCRSB5PERFORHRM4FWHNPAQ4YXDYKMQZ2IISJKR4UYZNX4US"
      eve.accountid.toString() shouldEqual "GBCJ6N6TUBLHKHSA5EDOTAWCQYEHBX6T2JX7FKGSQRGONAL7Z6TROSTT"

      tao.accountid.toString() shouldEqual "GAB2CB576PHBBPQ5ODORRZ2LYCMWPZGWGCN2KDK7DXOIMZASKUY3QZ6Q"
      yin.accountid.toString() shouldEqual "GC3BWHRPM3SQFRUCM3JY2TTHLETTXKCQNE2BJYFLJXRCEFIMAVQB552G"
      yang.accountid.toString() shouldEqual "GDHEFJQIKJ6JTTOOBTQHTXEXGQJHUK45JL5LYSR37O2SZLYLTUWR755D"
    }
    it should "parse correct string" in {
      val master2 = StrSeed.parse("SBQWY3DNPFWGSZTFNV4WQZLBOJ2GQYLTMJSWK3TTMVQXEY3INFXGO52X")
      master2.get.accountid.toString() shouldEqual "GCEZWKCA5VLDNRLN3RPRJMRZOX3Z6G5CHCGSNFHEYVXM3XOJMDS674JZ"
    }
    it should "throw when parse wrong string" in {
      val shorten_master = StrSeed.parse("SBQWY3DNPF")
      a [Exception] must be thrownBy shorten_master.get
      val typo_master = StrSeed.parse("SBQWY3DNPFWGSZTFNV4WQZLBOJ2GQYLTMJSWK3TTMVQXEY3INFXGO52Y")
      a [Exception] must be thrownBy typo_master.get
    }
    //TODO
    it should "correct misreading charater by base32 constraint" in {
      ////replace O=>0, I=>1, etc
      //val ret = StrSeed.parse("SBQWY3DNPFWGSZTFNV4WQZLBOJ2GQYLTMJSWK3TTMVQXEY3INFXGO52X", true)
    }

  }
}

object StrAccountIDSpec extends FlatSpec with Matchers {
  import org.strllar.stellarbase.Networks.XLMLive

  val test_account = new StrAccountID(1 to 32 map(_.toByte))
  it should "genderate correct address" in {
    test_account.toString()  shouldEqual "GAAQEAYEAUDAOCAJBIFQYDIOB4IBCEQTCQKRMFYYDENBWHA5DYPSABOV"
  }
  it should "parse correct string" in {
    val test_account2 = StrAccountID.parse("GAAQEAYEAUDAOCAJBIFQYDIOB4IBCEQTCQKRMFYYDENBWHA5DYPSABOV")
    test_account2.get.toString() shouldEqual "GAAQEAYEAUDAOCAJBIFQYDIOB4IBCEQTCQKRMFYYDENBWHA5DYPSABOV"
  }
}
