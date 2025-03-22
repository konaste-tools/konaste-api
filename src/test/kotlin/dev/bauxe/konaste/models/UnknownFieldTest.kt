package dev.bauxe.konaste.models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class UnknownFieldTest :
    FunSpec({
      test("Integer can convert to unknown field") {
        val result = UnknownField.fromInt(0x67AB1234)
        result.asInt1 shouldBe 0x67AB1234
        result.asInt2 shouldBe 0

        result.asFloat1 shouldBe 0x67AB1234
        result.asFloat2 shouldBe 0

        result.asLong shouldBe 0x0000000067AB1234
      }
      test("String is properly parsed from integer") {
        val result = UnknownField.fromInt(0x58564453)

        result.asString shouldBe "SDVX"
      }
      test("Long can convert to unknown field") {
        val result = UnknownField.fromLong(0x67AB12345678FFEE)
        result.asInt1 shouldBe 0x5678FFEE
        result.asInt2 shouldBe 0x67AB1234

        result.asFloat1 shouldBe 0x5678FFEE
        result.asFloat2 shouldBe 0x67AB1234

        result.asLong shouldBe 0x67AB12345678FFEE
      }
      test("String is properly parsed from long") {
        val result = UnknownField.fromLong(0x2144582158564453)

        result.asString shouldBe "SDVX!XD!"
      }
    }) {}
