package mytests

import com.inthenow.zcheck.{SpecLite}

import org.strllar.stellarbase.StrAddress
import org.strllar.stellarbase.legacy.StrKey.Converters._

object LegacyStrKeySpec extends SpecLite {

  "legacy keys" should {
    "be identity with decode+encode" in {
      "s3mH7MZ5ud38orpMFjXtUcu3jb3nhdQTPVC6Td4rVbi9biCm1W5".asLegacySeed.toString must_== "s3mH7MZ5ud38orpMFjXtUcu3jb3nhdQTPVC6Td4rVbi9biCm1W5"
      "gggGpCWrMJwngLjeWqa27BtMTkqmwfMvt".asLegacyAddress.toString must_== "gggGpCWrMJwngLjeWqa27BtMTkqmwfMvt"
    }

    def checkIsPairedLegacyKey(skey :String, addr :String): Unit = {
      skey.asLegacySeed.upgrade.address.legacy must_== addr
    }

    "should be paired" in {
      checkIsPairedLegacyKey("s3mH7MZ5ud38orpMFjXtUcu3jb3nhdQTPVC6Td4rVbi9biCm1W5", "gMCUfsYgEQZnhCR3cKrxnwnuDho9emWUf8")
      checkIsPairedLegacyKey("sfBuEPPnEQ67whwf8tpwkJdCTiA6TpC1FG5Jgkq9numMZ63YcPw", "gggR6rawTsCo6tBHx5RwyfgzHXbNYc64Z")
      checkIsPairedLegacyKey("sfBuLU86ZEWLomNrVZZWY8xf342Z6m8MgJeWNxZyRhZe4JhBVg9", "ggg9mWaz4MTyK2YJ8pT4smQ6r32xrB7Z7")
      checkIsPairedLegacyKey("sfBuWo2ENDLJ4gpjCaHPMaKSpPLSJXW3D2KvxqNFdP8hMxvgmPy", "gggGpCWrMJwngLjeWqa27BtMTkqmwfMvt")
    }

    "pass other tests" in {
      val abc = "s3mH7MZ5ud38orpMFjXtUcu3jb3nhdQTPVC6Td4rVbi9biCm1W5".asLegacyAddress.mustThrowA[Exception]
      "gggGpCWrMJwngLjeWqa27BtMTkqmwfMvt".asLegacySeed.mustThrowA[Exception]

      "GAKIEJNSVB44WDEVNDY6RGRVOXWHZWYM7V3UULOZBVISONXM76OYHRGU" .asStrAddress.legacy must_== "gUtknaqjh8LMRedgFLof2E2LZuiEihQDVD"
      "GB2Y4SUXOWSTXHTL7QVNGGK3Y6IOMUGHDB3ZICWJN2EJ2PJHORWS3LG4".asStrAddress.legacy  must_== "gNvsRBqV5YEarv7RCjuYzmsYoXDJpeHRtD"
    }
  }
}