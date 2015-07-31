/**
 * Created by kring on 2015/7/31.
 */
package org.strllar.stellarbase

import org.apache.commons.codec.binary.{Base32, Hex => ApacheHex}

object BaseN {
  val base32eng = new Base32
  val base32 = new {
    def encode(bs: Array[Byte]) = base32eng.encodeToString(bs)

    def decode(bs: String) = base32eng.decode(bs)
  }

  class BaseNString(val ms :Array[Byte], val nbit :Int) {
      //nbis: 4->hex; 5->base32; 6->base64; 8->raw
    override def equals(other :Any): Boolean = {
        false
      }
  }


}

object Hex {
  import BaseN.BaseNString

  //def normalize(sep:Option[Char] = ' ', cap:Boolean = false) = ""

  def apply(s:String) = new BaseNString(ApacheHex.decodeHex(s.toCharArray), 4)
  def apply(ch :Int*) = new BaseNString(ch.map(_.toByte).toArray, 4)
  def apply(bs :Array[Byte]) = new BaseNString(bs, 4)
}

object Lit {
  import BaseN.BaseNString
  lazy val base32eng = new Base32

  def apply(s:String) = new BaseNString(s.getBytes, 8) {
    def toBase32Raw = base32eng.encodeToString(ms)
    def toBase32 = new BaseNString(ms, 5)

    def asBase32 = Hex(base32eng.decode(ms))
  }
}