package org.strllar.stellarbase

package legacy {

import org.strllar.stellarbase.{RipeMD160, Sha256}

object StrKey {
  object VersionEncoding
  {
    val VER_NONE = 1
    val VER_NODE_PUBLIC = 122  // n
    val VER_NODE_PRIVATE = 102 //h
    val VER_ACCOUNT_ID = 0 // g
    val VER_ACCOUNT_PUBLIC = 67 //p
    val VER_ACCOUNT_PRIVATE = 101 //h
    val VER_SEED = 33 // s
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

  def SHA256(payload :IndexedSeq[Byte]) :Vector[Byte] = {
    val buff = payload.toArray
    val md256 = new Sha256()
    md256.update(buff)
    Vector(md256.digest:_*)
  }
  def SHA256Hash(payload :IndexedSeq[Byte]) :Vector[Byte] = {
    //SHA256Hash: double sha256
    val hash256 = SHA256(payload).toArray
    val md256 = new Sha256()
    md256.update(hash256)
    Vector(md256.digest:_*)
  }
  def Hash160(payload :IndexedSeq[Byte]) :Vector[Byte] = {
    val hash256 = SHA256(payload).toArray
    val md160 = new RipeMD160
    md160.update(hash256)
    Vector(md160.digest:_*)
  }

  def encodeCheck(payload :IndexedSeq[Byte], version :Byte) :String = {
    val buff = new Array[Byte](1 + payload.length + 4)
    buff(0) = version
    payload.copyToArray(buff, 1)

    val hash4  = SHA256Hash(buff.dropRight(4)).take(4)
    hash4.copyToArray(buff, payload.length+1)

    Base58en(buff)
  }

  def decodeCheck(chars :String) :Vector[Byte] = {
    val bytes = Base58de(chars)
    val ver = bytes.head
    val body = bytes.dropRight(4)
    val checksum = bytes.takeRight(4)

    val hash4 = SHA256Hash(body).take(4)
    assert(hash4 == checksum) //TODO
    body.tail //payload
  }

  def seed(bytes :Array[Byte]) = {
    encodeCheck(bytes, VersionEncoding.VER_SEED.toByte)
  }

  def address(pubkey :Array[Byte]) = {
    val accountid = Hash160(pubkey)
    encodeCheck(accountid, VersionEncoding.VER_ACCOUNT_ID.toByte)
  }

  def parseSeed(skey :String): Unit = {

  }

  def parseAddres(gkey :String): Unit ={

  }
}
}