import com.inthenow.zcheck.{SpecLite}
import org.strllar.stellarbase.{StrSeed, StrAccountID, QuorumSet}


object QuorumSetSpec extends SpecLite {
  import org.strllar.stellarbase.Networks.XLMLive

  " Simple Validator QuorumSet" should {
    "generate config string" in {
      val generated = QuorumSet.toConfigString(QuorumSet(Seq(StrSeed.parse("SDQVDISRYN2JXBS7ICL7QJAEKB3HWBJFP2QECXG7GZICAHBK4UNJCWK2").get.accountid), Seq.empty, 88))
      val ref =
      """[QUORUM_SET]
        |THRESHOLD_PERCENT=88
        |VALIDATORS=[
        |"GCTI6HMWRH2QGMFKWVU5M5ZSOTKL7P7JAHZDMJJBKDHGWTEC4CJ7O3DU"]
      |""".stripMargin
      generated.lines.zip(ref.lines).foreach((x) => x._1 must_== x._2)
    }
  }

  " Three Validator QuorumSet" should {
    "generate config string" in {
      val generated = QuorumSet.toConfigString(QuorumSet(Seq(
        StrAccountID.parse("GDKXE2OZMJIPOSLNA6N6F2BVCI3O777I2OOC4BV7VOYUEHYX7RTRYA7Y").get,
        StrAccountID.parse("GCUCJTIYXSOXKBSNFGNFWW5MUQ54HKRPGJUTQFJ5RQXZXNOLNXYDHRAP").get,
        StrAccountID.parse("GC2V2EFSXN6SQTWVYA5EPJPBWWIMSD2XQNKUOHGEKB535AQE2I6IXV2Z").get
      ), Seq.empty, 100))
      val ref =
        s"""[QUORUM_SET]
           |THRESHOLD_PERCENT=100
           |VALIDATORS=[
           |"GDKXE2OZMJIPOSLNA6N6F2BVCI3O777I2OOC4BV7VOYUEHYX7RTRYA7Y",
           |"GCUCJTIYXSOXKBSNFGNFWW5MUQ54HKRPGJUTQFJ5RQXZXNOLNXYDHRAP",
           |"GC2V2EFSXN6SQTWVYA5EPJPBWWIMSD2XQNKUOHGEKB535AQE2I6IXV2Z"]
           |""".stripMargin

      generated.lines.zip(ref.lines).foreach((x) => x._1 must_== x._2)
    }
  }

  "Two Level QuorumSet" should {
    "generate config string" in {
      val generated = QuorumSet.toConfigString(QuorumSet(Seq(
        StrAccountID.parse("GDKXE2OZMJIPOSLNA6N6F2BVCI3O777I2OOC4BV7VOYUEHYX7RTRYA7Y").get
      ), Seq(
        QuorumSet(Seq(
          StrAccountID.parse("GCUCJTIYXSOXKBSNFGNFWW5MUQ54HKRPGJUTQFJ5RQXZXNOLNXYDHRAP").get,
          StrAccountID.parse("GC2V2EFSXN6SQTWVYA5EPJPBWWIMSD2XQNKUOHGEKB535AQE2I6IXV2Z").get
        ))
      ), 100))
      val ref =
        """[QUORUM_SET]
          |THRESHOLD_PERCENT=100
          |VALIDATORS=[
          |"GDKXE2OZMJIPOSLNA6N6F2BVCI3O777I2OOC4BV7VOYUEHYX7RTRYA7Y"]
          |[QUORUM_SET.1]
          |VALIDATORS=[
          |"GCUCJTIYXSOXKBSNFGNFWW5MUQ54HKRPGJUTQFJ5RQXZXNOLNXYDHRAP",
          |"GC2V2EFSXN6SQTWVYA5EPJPBWWIMSD2XQNKUOHGEKB535AQE2I6IXV2Z"]
          |""".stripMargin

      generated.lines.zip(ref.lines).foreach((x) => x._1 must_== x._2)
    }
  }

  "Full Level QuorumSet" should {
    "generate config string" in {
      val generated = QuorumSet.toConfigString(
        QuorumSet(Seq(//Root
          StrAccountID.parse("GDQWITFJLZ5HT6JCOXYEVV5VFD6FTLAKJAUDKHAV3HKYGVJWA2DPYSQV").get,
          StrAccountID.parse("GANLKVE4WOTE75MJS6FQ73CL65TSPYYMFZKC4VDEZ45LGQRCATGAIGIA").get,
          StrAccountID.parse("GDV46EIEF57TDL4W27UFDAUVPDDCKJNVBYB3WIV2WYUYUG753FCFU6EJ").get
        ), Seq(
          QuorumSet(Seq(//.1
            StrAccountID.parse("GDKYAJOBUIXSFGGE3EPBSGZD7JT2YKMFLELG6A27LUCZWH4T52TPP6LH").get,
            StrAccountID.parse("GDXJAZZJ3H5MJGR6PDQX3JHRREAVYNCVM7FJYGLZJKEHQV2ZXEUO5SX2").get,
            StrAccountID.parse("GB6GK3WWTZYY2JXWM6C5LRKLQ2X7INQ7IYTSECCG3SMZFYOZNEZR4SO5").get
          ), Seq.empty, 67),
          QuorumSet(Seq(//.2
            StrAccountID.parse("GCTAIXWDDBM3HBDHGSAOLY223QZHPS2EDROF7YUBB3GNYXLOCPV5PXUK").get,
            StrAccountID.parse("GCJ6UBAOXNQFN3HGLCVQBWGEZO6IABSMNE2OCQC4FJAZXJA5AIE7WSPW").get
          ), Seq(
            QuorumSet(Seq(//.2.1
              StrAccountID.parse("GC4X65TQJVI3OWAS4DTA2EN2VNZ5ZRJD646H5WKEJHO5ZHURDRAX2OTH").get,
              StrAccountID.parse("GAXSWUO4RBELRQT5WMDLIKTRIKC722GGXX2GIGEYQZDQDLOTINQ4DX6F").get,
              StrAccountID.parse("GAWOEMG7DQDWHCFDTPJEBYWRKUUZTX2M2HLMNABM42G7C7IAPU54GL6X").get,
              StrAccountID.parse("GDZAJNUUDJFKTZX3YWZSOAS4S4NGCJ5RQAY7JPYBG5CUFL3JZ5C3ECOH").get
            ), Seq.empty, 50)
          ), 100)
        ))
      )

      val ref =
        (
          s"""[QUORUM_SET]
             |VALIDATORS=[
             |"GDQWITFJLZ5HT6JCOXYEVV5VFD6FTLAKJAUDKHAV3HKYGVJWA2DPYSQV",
             |"GANLKVE4WOTE75MJS6FQ73CL65TSPYYMFZKC4VDEZ45LGQRCATGAIGIA",
             |"GDV46EIEF57TDL4W27UFDAUVPDDCKJNVBYB3WIV2WYUYUG753FCFU6EJ"]
             |[QUORUM_SET.1]
             |THRESHOLD_PERCENT=67
             |VALIDATORS=[
             |"GDKYAJOBUIXSFGGE3EPBSGZD7JT2YKMFLELG6A27LUCZWH4T52TPP6LH",
             |"GDXJAZZJ3H5MJGR6PDQX3JHRREAVYNCVM7FJYGLZJKEHQV2ZXEUO5SX2",
             |"GB6GK3WWTZYY2JXWM6C5LRKLQ2X7INQ7IYTSECCG3SMZFYOZNEZR4SO5"]
             |[QUORUM_SET.2]
             |THRESHOLD_PERCENT=100
             |VALIDATORS=[
             |"GCTAIXWDDBM3HBDHGSAOLY223QZHPS2EDROF7YUBB3GNYXLOCPV5PXUK",
             |"GCJ6UBAOXNQFN3HGLCVQBWGEZO6IABSMNE2OCQC4FJAZXJA5AIE7WSPW"]
             |[QUORUM_SET.2.1]
             |THRESHOLD_PERCENT=50
             |VALIDATORS=[
             |"GC4X65TQJVI3OWAS4DTA2EN2VNZ5ZRJD646H5WKEJHO5ZHURDRAX2OTH",
             |"GAXSWUO4RBELRQT5WMDLIKTRIKC722GGXX2GIGEYQZDQDLOTINQ4DX6F",
             |"GAWOEMG7DQDWHCFDTPJEBYWRKUUZTX2M2HLMNABM42G7C7IAPU54GL6X",
             |"GDZAJNUUDJFKTZX3YWZSOAS4S4NGCJ5RQAY7JPYBG5CUFL3JZ5C3ECOH"]
           |""".stripMargin

          )

      generated.lines.zip(ref.lines).foreach((x) => x._1 must_== x._2)
    }
  }
}