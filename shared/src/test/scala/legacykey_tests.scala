package mytests

import com.inthenow.zcheck.{SpecLite}

import org.strllar.stellarbase.{StrSeed, StrKey}
import org.strllar.stellarbase.legacy.{StrKey => LegacyStrKey}

object LegacyStrKeySpec extends SpecLite {

  def isPairedLegacyKey(skey :String, addr :String): Unit = {
    val someseed = LegacyStrKey.decodeCheck(skey)
    val pubkey = StrSeed(someseed toArray).address.byteBuffer
    val someaddr = LegacyStrKey.address(pubkey)
    someaddr must_== addr
  }

  "sandbox here" in {

    isPairedLegacyKey("s3mH7MZ5ud38orpMFjXtUcu3jb3nhdQTPVC6Td4rVbi9biCm1W5", "gMCUfsYgEQZnhCR3cKrxnwnuDho9emWUf8")
    isPairedLegacyKey("sfBuEPPnEQ67whwf8tpwkJdCTiA6TpC1FG5Jgkq9numMZ63YcPw", "gggR6rawTsCo6tBHx5RwyfgzHXbNYc64Z")
    isPairedLegacyKey("sfBuLU86ZEWLomNrVZZWY8xf342Z6m8MgJeWNxZyRhZe4JhBVg9", "ggg9mWaz4MTyK2YJ8pT4smQ6r32xrB7Z7")
    isPairedLegacyKey("sfBuWo2ENDLJ4gpjCaHPMaKSpPLSJXW3D2KvxqNFdP8hMxvgmPy", "gggGpCWrMJwngLjeWqa27BtMTkqmwfMvt")

//    val seed = StrKey.random()
//    val addr = seed.address
//    println(seed)
//    println(addr)
//    println(LegacyStrKey.seed(seed.rawseed))
//    println(LegacyStrKey.address(addr.byteBuffer))

  }
}