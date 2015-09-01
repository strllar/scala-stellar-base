/**
 * Created by kring on 2015/8/31.
 */
package org.strllar.stellarbase

case class QuorumSet(valids: Seq[StrAddress], innners: Seq[QuorumSet] = Seq.empty, threshhold: Int = 0)

object QuorumSet {
  def apply(valids :StrAddress*):QuorumSet = {
    QuorumSet(valids.toSeq)
  }
  def apply(valids: Seq[StrAddress], threshhold: Int, inners :QuorumSet*):QuorumSet = {
   QuorumSet(valids, inners.toSeq, threshhold)
  }

  def toConfigString(qset: QuorumSet, levels: Seq[Int] = Seq.empty): String = {
    val thread = levels.map("."+_).mkString
    s"""[QUORUM_SET$thread]
       |""".stripMargin ++
      (if (qset.threshhold != 0)
        s"""THRESHOLD_PERCENT=${qset.threshhold}
           |""".stripMargin else "") ++
      s"""VALIDATORS=[
       |""".stripMargin ++
      qset.valids.map(v => s""""${v.toString}"""").mkString("", ",\n", "]\n") ++
      qset.innners.zipWithIndex.map { x =>
        val (qs, id) = x
        toConfigString(qs, levels :+ (id+1))
      }.mkString
  }

}