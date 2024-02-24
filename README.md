### Echelon

*Echelon* is a simple programming language designed to be small in size and easy to implement.

### How to start

Once you built and run the program, you will be able to write programs (although only one-liners if you are using the provided command line interface without modifications).

For a program you need to define a function, you can do it using this syntax: (if you are using it in command line use `\` instead of new lines, they serve the same purpose)
```
!
 <code goes here>
~
```

### Syntax
Shown in previous example: `!` defines a function, and `~` returns it. You can define a function with a name, arguments and return value like this:
```
!name:arg1,arg2
 <code goes here>
~returnvalue
```
Lack of name defines main function.

Identifiers (used to define function and variable names) are not bound to just letters or numbers, they use any symbols as long as you can't confuse them with keywords (key-characters?). You can use `name999`, `$$$`, `(yes/that+too!!)`, but not `!a`, `~~` or `123`

You can define builting functions as shown in [Main](src/main/java/me/white/echelon/Main.java) file

#### Types
There are 3 value types in *echenlon*: String, Number and Container

String acts as usual, some text surrounded by 2 `"`

Numbers only represent 32-bit integer values at the moment

Containers are sort of variable, explained below:

```
!
 =:x,10
 y:x
 <:x
~

!y:x
 =:x,9
~
```
In this example, main function assigns container `x` to number `10` via *builtin function*, sends `x` to function `y` where it gets redefined to `9` and outputs the value of `x`, which is, as changed by the function `y`, equals to `9`

This happens because function `y` is modifying its argument directly before using. To avoid that you can copy the value of `x` to another container:
```
!y:x
 =:z,x
 =:z,9
~
```
That way value of `x` is not changed, because function `y` is modifying container defined within function `y`

#### Instructions

Instructions are nested function calls that drive the function. You can define as many instructions in a function as you want

Let's look at this example: `x:a,y:b,c`, with functions `x` and `y` that take 2 arguments each

This instruction calls 2 functions: `x` and `y`, with 3 arguments: `a`, `b` and `c`. Both of these functions take 2 arguments, meaning `x` takes `a` and the result of `y`, which takes values of `b` and `c`

Let's define (perhaps, builtin) function `+`, that takes 2 arguments and returns their sum. How do we get sum of 3 numbers, or even 4? We can easily use this nested property of instructions to do something like this:

`+:a,+:b,+:c,d`, or even better: `+:+:+:a,b,c,d`.

Here's the same example but highlighting what functions get what arguments: `+(a,+(b,+(c,d)))`, `+(+(+(a,b),c),d)`
