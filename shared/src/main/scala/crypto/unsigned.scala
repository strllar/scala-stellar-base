package org.strllar.stellarbase

object UnsignedOps {
  def unsignedValue(b :Byte) :Short = (0xff & b).toShort
  def unsignedValue(s :Short) :Int = 0xffff & s
  def unsignedValue(i :Int) :Long = 0xffffffffL & i
  def unsignedValue(unsignedLong: Long) :BigInt = (BigInt(unsignedLong >>> 1) << 1) + (unsignedLong & 1)
}