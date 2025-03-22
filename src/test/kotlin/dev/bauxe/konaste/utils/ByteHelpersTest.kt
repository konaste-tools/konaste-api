package dev.bauxe.konaste.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual

class ByteHelpersTest :
    FunSpec({
      test("Converts int into little-endian long") {
        val result =
            ByteHelpers.littleEndianByteConversion(
                byteArrayOf(0xc1.toByte(), 0xf0.toByte(), 0x8f.toByte(), 0x40.toByte()))
        result shouldBeEqual 0x408ff0c1L
      }

      test("Converts short into little-endian long") {
        val result =
            ByteHelpers.littleEndianByteConversion(byteArrayOf(0x9a.toByte(), 0x96.toByte()))
        result shouldBeEqual 0x969aL
      }

      test("Converts byte into little-endian long") {
        val result = ByteHelpers.littleEndianByteConversion(byteArrayOf(0x1d.toByte()))
        result shouldBeEqual 0x1dL
      }

      test("Converts long into little-endian long") {
        val result =
            ByteHelpers.littleEndianByteConversion(
                byteArrayOf(
                    0xb3.toByte(),
                    0x38.toByte(),
                    0x84.toByte(),
                    0xd7.toByte(),
                    0x57.toByte(),
                    0xec.toByte(),
                    0x1b.toByte(),
                    0xfa.toByte()))
        result shouldBeEqual 0xfa1bec57d78438b3UL.toLong()
      }
    }) {}
