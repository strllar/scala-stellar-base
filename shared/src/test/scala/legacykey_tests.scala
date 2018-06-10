package mytests

import org.scalatest._

import org.strllar.stellarbase.legacy.StrKey.Converters._

object LegacyStrKeySpec extends FlatSpec with Matchers {
  import org.strllar.stellarbase.Networks.XLMLive

  "legacy keys" should "pass all" in{
    it should "be identity with decode+encode" in {
      "s3mH7MZ5ud38orpMFjXtUcu3jb3nhdQTPVC6Td4rVbi9biCm1W5".asLegacySeed.toString shouldEqual "s3mH7MZ5ud38orpMFjXtUcu3jb3nhdQTPVC6Td4rVbi9biCm1W5"
      "gggGpCWrMJwngLjeWqa27BtMTkqmwfMvt".asLegacyAddress.toString shouldEqual "gggGpCWrMJwngLjeWqa27BtMTkqmwfMvt"
    }

    def checkIsPairedLegacyKey(skey :String, addr :String): Unit = {
      skey.asLegacySeed.upgrade.accountid.legacy shouldEqual addr
    }

    it should "be paired" in {
      checkIsPairedLegacyKey("s3mH7MZ5ud38orpMFjXtUcu3jb3nhdQTPVC6Td4rVbi9biCm1W5", "gMCUfsYgEQZnhCR3cKrxnwnuDho9emWUf8")
      checkIsPairedLegacyKey("sfBuEPPnEQ67whwf8tpwkJdCTiA6TpC1FG5Jgkq9numMZ63YcPw", "gggR6rawTsCo6tBHx5RwyfgzHXbNYc64Z")
      checkIsPairedLegacyKey("sfBuLU86ZEWLomNrVZZWY8xf342Z6m8MgJeWNxZyRhZe4JhBVg9", "ggg9mWaz4MTyK2YJ8pT4smQ6r32xrB7Z7")
      checkIsPairedLegacyKey("sfBuWo2ENDLJ4gpjCaHPMaKSpPLSJXW3D2KvxqNFdP8hMxvgmPy", "gggGpCWrMJwngLjeWqa27BtMTkqmwfMvt")
    }

    it should "pass other tests" in {
      a [Exception] should be thrownBy (
        "s3mH7MZ5ud38orpMFjXtUcu3jb3nhdQTPVC6Td4rVbi9biCm1W5".asLegacyAddress
      )
      a [Exception] should be thrownBy (
        "gggGpCWrMJwngLjeWqa27BtMTkqmwfMvt".asLegacySeed
        )
      "GAKIEJNSVB44WDEVNDY6RGRVOXWHZWYM7V3UULOZBVISONXM76OYHRGU" .asStrAccountID.legacy shouldEqual "gUtknaqjh8LMRedgFLof2E2LZuiEihQDVD"
      "GB2Y4SUXOWSTXHTL7QVNGGK3Y6IOMUGHDB3ZICWJN2EJ2PJHORWS3LG4".asStrAccountID.legacy  shouldEqual "gNvsRBqV5YEarv7RCjuYzmsYoXDJpeHRtD"
    }
  }
}