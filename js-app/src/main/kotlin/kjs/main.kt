package kjs

fun main() {
    println(defaultGreet("John"))
    println(greet("John"))
    println(advocate(42))
    println(RandomStringFromNpm.generate()) // length by default is 32
    println(RandomStringFromNpm.generate(3))
    println(sorted(arrayOf(1, 2, 3)))
    println(sorted(arrayOf(3, 2, 1)))
}
