package com.example.matlabcalculator

import kotlin.math.*

class Evaluator(private val variables: Map<String, Double> = emptyMap()) {

    private var pos = 0
    private var cleanExpr = ""

    private fun peek(): Char? = if (pos < cleanExpr.length) cleanExpr[pos] else null
    
    private fun nextChar(): Char? {
        val char = peek()
        if (char != null) pos++
        return char
    }

    private fun match(expected: Char): Boolean {
        if (peek() == expected) {
            pos++
            return true
        }
        return false
    }

    fun evaluate(expression: String): Double {
        // Convert 'mod' to '%' for easier tokenization/parsing
        cleanExpr = expression.replace(" ", "").lowercase().replace("mod", "%")
        pos = 0
        val result = parseExpression()
        if (pos < cleanExpr.length) {
            throw IllegalArgumentException("Unexpected symbol at end: '${cleanExpr.substring(pos)}'")
        }
        return result
    }

    // expression = term | expression '+' term | expression '-' term
    private fun parseExpression(): Double {
        var result = parseTerm()
        while (true) {
            if (match('+')) {
                result += parseTerm()
            } else if (match('-')) {
                result -= parseTerm()
            } else {
                break
            }
        }
        return result
    }

    // term = factor | term '*' factor | term '/' factor | term '%' factor
    private fun parseTerm(): Double {
        var result = parsePower()
        while (true) {
            if (match('*')) {
                result *= parsePower()
            } else if (match('/')) {
                val divisor = parsePower()
                if (divisor == 0.0) throw ArithmeticException("Division by zero")
                result /= divisor
            } else if (match('%')) {
                val divisor = parsePower()
                if (divisor == 0.0) throw ArithmeticException("Modulo by zero")
                result %= divisor
            } else {
                break
            }
        }
        return result
    }

    // power = factor [ '^' power ] (right-associative)
    private fun parsePower(): Double {
        var result = parseFactor()
        if (match('^')) {
            result = result.pow(parsePower())
        }
        return result
    }

    // factor = (number | '(' expression ')' | '-' factor | '+' factor | function | variable) [ '!' ]
    private fun parseFactor(): Double {
        if (match('-')) return -parseFactor()
        if (match('+')) return parseFactor()

        val startPos = pos
        val ch = peek() ?: throw IllegalArgumentException("Unexpected end of expression")

        var result: Double

        if (ch == '(') {
            nextChar() // Consume '('
            result = parseExpression()
            if (!match(')')) throw IllegalArgumentException("Missing closing parenthesis")
        } else if (ch.isDigit() || ch == '.') {
            // Parse numbers
            while (peek()?.isDigit() == true || peek() == '.') {
                nextChar()
            }
            val numStr = cleanExpr.substring(startPos, pos)
            result = numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $numStr")
        } else if (ch.isLetter()) {
            // Parse identifiers (functions, variables, constants)
            while (peek()?.isLetter() == true) {
                nextChar()
            }
            val name = cleanExpr.substring(startPos, pos)

            if (name == "pi") {
                result = PI
            } else if (name == "e") {
                result = E
            } else if (variables.containsKey(name)) {
                result = variables[name]!!
            } else if (match('(')) {
                val arg = parseExpression()
                if (!match(')')) throw IllegalArgumentException("Missing closing parenthesis for function $name")
                result = when (name) {
                    "sin" -> sin(arg)
                    "cos" -> cos(arg)
                    "tan" -> tan(arg)
                    "asin" -> asin(arg)
                    "acos" -> acos(arg)
                    "atan" -> atan(arg)
                    "sqrt" -> {
                        if (arg < 0.0) throw ArithmeticException("Square root of negative number")
                        sqrt(arg)
                    }
                    "log" -> log10(arg)
                    "ln" -> ln(arg)
                    "abs" -> abs(arg)
                    "exp" -> exp(arg)
                    else -> throw IllegalArgumentException("Unknown function: $name")
                }
            } else {
                throw IllegalArgumentException("Unknown identifier: $name")
            }
        } else {
            throw IllegalArgumentException("Unexpected character: '$ch'")
        }

        // Handle postfix factorial
        while (match('!')) {
            result = factorial(result)
        }

        return result
    }

    private fun factorial(n: Double): Double {
        if (n < 0.0 || n % 1.0 != 0.0) {
            throw ArithmeticException("Factorial is only defined for non-negative integers")
        }
        val intN = n.toInt()
        if (intN > 170) {
            throw ArithmeticException("Factorial overflow (>170!)")
        }
        var fact = 1.0
        for (i in 2..intN) {
            fact *= i
        }
        return fact
    }
}
