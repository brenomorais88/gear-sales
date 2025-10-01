package com.sales.features.util

internal object DocumentoBR {
    fun normalize(doc: String) = doc.filter(Char::isDigit)

    fun isValidCPFOrCNPJ(doc: String): Boolean {
        val n = normalize(doc)
        return when (n.length) {
            11 -> isValidCPF(n)
            14 -> isValidCNPJ(n)
            else -> false
        }
    }

    private fun isValidCPF(cpf: String): Boolean {
        if (cpf.length != 11) return false
        if ((0..9).any { d -> cpf.all { it == ('0' + d) } }) return false

        fun dv(base: String, factorStart: Int): Int {
            var factor = factorStart
            val sum = base.sumOf { (it - '0') * factor-- }
            val mod = sum % 11
            return if (mod < 2) 0 else 11 - mod
        }
        val d1 = dv(cpf.substring(0, 9), 10)
        val d2 = dv(cpf.substring(0, 9) + d1, 11)
        return cpf.endsWith("$d1$d2")
    }

    private fun isValidCNPJ(cnpj: String): Boolean {
        if (cnpj.length != 14) return false
        if ((0..9).any { d -> cnpj.all { it == ('0' + d) } }) return false

        val pesos1 = intArrayOf(5,4,3,2,9,8,7,6,5,4,3,2)
        val pesos2 = intArrayOf(6,5,4,3,2,9,8,7,6,5,4,3,2)

        fun calc(base: String, pesos: IntArray): Int {
            val sum = base.mapIndexed { i, c -> (c - '0') * pesos[i] }.sum()
            val mod = sum % 11
            return if (mod < 2) 0 else 11 - mod
        }

        val d1 = calc(cnpj.substring(0, 12), pesos1)
        val d2 = calc(cnpj.substring(0, 12) + d1, pesos2)
        return cnpj.endsWith("$d1$d2")
    }
}