package org.strllar.stellarbase

package legacy {

import org.strllar.stellarbase.{RipeMD160, Sha256}
import util.{Try, Failure, Success}

object StrKey {
  object VersionEncoding
  {
    val VER_NONE :Byte = 1
    val VER_NODE_PUBLIC :Byte = 122  // n
    val VER_NODE_PRIVATE :Byte = 102 //h
    val VER_ACCOUNT_ID :Byte = 0 // g
    val VER_ACCOUNT_PUBLIC :Byte = 67 //p
    val VER_ACCOUNT_PRIVATE :Byte = 101 //h
    val VER_SEED :Byte = 33 // s
  } ;
  val alphabet = "gsphnaf39wBUDNEGHJKLM4PQRST7VWXYZ2bcdeCr65jkm8oFqi1tuvAxyz"
  val revalphabet = alphabet.zipWithIndex.toMap

  private[this] def unsignedBN(input: Array[Byte]) = BigInt(1, input)
  private[this] def Base58en(bytes: Seq[Byte]) :String = {
    assert(alphabet.length == 58)
    //flowing non-rfc general basen logic
    val bn = unsignedBN(bytes.toArray)
    val base = BigInt(alphabet.length)
    val charsvec = Iterator.iterate(bn)(_ / base).takeWhile(_ > 0).map(x => alphabet.charAt((x % base).toInt)).toVector.reverse
    //prepend leading zeros
    alphabet.take(1)*(bytes.takeWhile(_ == 0).length) ++ charsvec
  }
  private[this] def Base58de(chars: String) :Vector[Byte] = {
    assert(revalphabet.size == 58)
    //flowing non-rfc general basen logic
    val bn = chars.foldLeft(BigInt(0))((n, c) => {
      n * revalphabet.size + revalphabet(c)
    })
    //restore leading zeros
    Vector.fill(chars.takeWhile(_ == alphabet.charAt(0)).length)(0.toByte) ++ bn.toByteArray
  }

  private[this] def SHA256(payload :IndexedSeq[Byte]) :Vector[Byte] = {
    val md256 = new Sha256()
    md256.update(payload)
    Vector(md256.digest:_*)
  }
  private[this] def SHA256Hash(payload :IndexedSeq[Byte]) :Vector[Byte] = {
    //SHA256Hash: double sha256
    val hash256 = SHA256(payload)
    val md256 = new Sha256()
    md256.update(hash256)
    Vector(md256.digest:_*)
  }
  private[this] def Hash160(payload :IndexedSeq[Byte]) :Vector[Byte] = {
    val hash256 = SHA256(payload)
    val md160 = new RipeMD160
    md160.update(hash256)
    Vector(md160.digest:_*)
  }

  private[this] def encodeCheck(payload :IndexedSeq[Byte], version :Byte) :String = {
    val buff = new Array[Byte](1 + payload.length + 4)
    buff(0) = version
    payload.copyToArray(buff, 1)

    val hash4  = SHA256Hash(buff.dropRight(4)).take(4)
    hash4.copyToArray(buff, payload.length+1)

    Base58en(buff)
  }

  private[this] def decodeCheck(chars :String, demandver :Byte) :Try[Vector[Byte]] = {
    Try(Base58de(chars)).flatMap((bytes) => {
      val ver = bytes.head
      val body = bytes.dropRight(4)
      val checksum = bytes.takeRight(4)
      val hash4 = SHA256Hash(body).take(4)

      if (ver != demandver) Failure(new Exception("wrong version"))
      else if (hash4 != checksum) Failure(new Exception("wrong crc"))
      else Success(body.tail) //payload
    })
  }

  def seed(bytes :IndexedSeq[Byte]) = {
    encodeCheck(bytes, VersionEncoding.VER_SEED)
  }

  def address(pubkey :IndexedSeq[Byte]) = {
    val accountid = Hash160(pubkey)
    encodeCheck(accountid, VersionEncoding.VER_ACCOUNT_ID)
  }

  object Converters {
    import org.strllar.stellarbase.{StrSeed, StrAddress}

    implicit def str2keys(rawstr :String)(implicit network :Network) = {
      new {
        def asLegacySeed = new {
          private[this] val tmpseed = decodeCheck(rawstr, VersionEncoding.VER_SEED).get
          def upgrade = new StrSeed(tmpseed) {
            def legacy = seed(rawseed)
          }
          override def toString = seed(tmpseed)
        }

        def asLegacyAddress = new {
          val rawhash160 = decodeCheck(rawstr, VersionEncoding.VER_ACCOUNT_ID).get
          override def toString = encodeCheck(rawhash160, VersionEncoding.VER_ACCOUNT_ID)
        }

        def asStrSeed = StrSeed.parse(rawstr).get
        def asStrAddress = StrAddress.parse(rawstr).get
      }
    }

    implicit def legacybridge(gkey :StrAddress) = new {
      def legacy = address(gkey.rawbytes)
    }
    implicit def legacybridge(skey :StrSeed) = new {
      def legacy =seed(skey.rawseed)
    }
  }
}
}