package com.myhome.rpgkeyboard.keyboardview

import android.os.Build
import android.util.Log
import android.view.inputmethod.InputConnection

open class HangulMaker {
    private var cho: Char = '\u0000'
    private var jun: Char = '\u0000'
    private var jon: Char = '\u0000'
    private var jonFlag:Char = '\u0000'
    private var doubleJonFlag:Char = '\u0000'
    var junFlag:Char = '\u0000'

    private val chos: List<Int> = listOf(0x3131, 0x3132, 0x3134, 0x3137, 0x3138, 0x3139, 0x3141,0x3142, 0x3143, 0x3145, 0x3146, 0x3147, 0x3148, 0x3149, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e)
    private val juns:List<Int> = listOf(0x314f, 0x3150, 0x3151, 0x3152, 0x3153, 0x3154, 0x3155, 0x3156, 0x3157, 0x3158, 0x3159, 0x315a, 0x315b, 0x315c, 0x315d, 0x315e, 0x315f, 0x3160, 0x3161, 0x3162, 0x3163)
    private val jons:List<Int> = listOf(0x0000, 0x3131, 0x3132, 0x3133, 0x3134, 0x3135, 0x3136, 0x3137, 0x3139, 0x313a, 0x313b, 0x313c, 0x313d, 0x313e, 0x313f, 0x3140, 0x3141, 0x3142, 0x3144, 0x3145, 0x3146, 0x3147, 0x3148, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e)

    /**
     * 0:""
     * 1: 모음 입력상태
     * 2: 모음 + 자음 입력상태
     * 3: 모음 + 자음 + 모음입력상태(초 중 종성)
     * 초성과 종성에 들어갈 수 있는 문자가 다르기 때문에 필요에 맞게 수정이 필요함.(chos != jons)
     */
    protected var state = 0
    private lateinit var inputConnection:InputConnection

    constructor(inputConnection: InputConnection){
        this.inputConnection = inputConnection
    }
    fun clear(){
        cho = '\u0000'
        jun = '\u0000'
        jon = '\u0000'
        jonFlag = '\u0000'
        doubleJonFlag = '\u0000'
        junFlag = '\u0000'
    }

    fun makeHan():Char{
        if(state == 0){
            return '\u0000'
        }
        if(state == 1){
            return cho
        }
        val choIndex = chos.indexOf(cho.toInt())
        val junIndex = juns.indexOf(jun.toInt())
        val jonIndex = jons.indexOf(jon.toInt())

        val makeResult = 0xAC00 + 28 * 21 * (choIndex) + 28 * (junIndex)  + jonIndex

        return makeResult.toChar()
    }

    open fun commit(c:Char){
        if(chos.indexOf(c.toInt()) < 0 && juns.indexOf(c.toInt()) < 0 && jons.indexOf(c.toInt()) < 0){
            directlyCommit()
            inputConnection.commitText(c.toString(), 1)
            return
        }
        when(state){
            0 -> {
                if(juns.indexOf(c.toInt()) >= 0){
                    inputConnection.commitText(c.toString(), 1)
                    clear()
                }else{//초성일 경우
                    state = 1
                    cho = c
                    inputConnection.setComposingText(cho.toString(), 1)
                }
            }
            1 -> {
                if(chos.indexOf(c.toInt()) >= 0){
                    inputConnection.commitText(cho.toString(), 1)
                    clear()
                    cho = c
                    inputConnection.setComposingText(cho.toString(), 1)
                }else{//중성일 경우
                    state = 2
                    jun = c
                    inputConnection.setComposingText(makeHan().toString(), 1)
                }
            }
            2 -> {
                if(juns.indexOf(c.toInt()) >= 0){
                    if(doubleJunEnable(c)){
                        inputConnection.setComposingText(makeHan().toString(), 1)
                    }
                    else{
                        inputConnection.commitText(makeHan().toString(), 1)
                        inputConnection.commitText(c.toString(), 1)
                        clear()
                        state = 0
                    }
                }
                else if(jons.indexOf(c.toInt()) >= 0){//종성이 들어왔을 경우
                    jon = c
                    inputConnection.setComposingText(makeHan().toString(), 1)
                    state = 3
                }
                else{
                    directlyCommit()
                    cho = c
                    state = 1
                    inputConnection.setComposingText(makeHan().toString(), 1)
                }
            }
            3 -> {
                if(jons.indexOf(c.toInt()) >= 0){
                    if(doubleJonEnable(c)){
                        inputConnection.setComposingText(makeHan().toString(), 1)
                    }
                    else{
                        inputConnection.commitText(makeHan().toString(), 1)
                        clear()
                        state = 1
                        cho = c
                        inputConnection.setComposingText(cho.toString(), 1)
                    }

                }
                else if(chos.indexOf(c.toInt()) >= 0){
                    inputConnection.commitText(makeHan().toString(), 1)
                    state = 1
                    clear()
                    cho = c
                    inputConnection.setComposingText(cho.toString(), 1)
                }
                else{//중성이 들어올 경우
                    var temp:Char = '\u0000'
                    if(doubleJonFlag == '\u0000'){
                        temp = jon
                        jon = '\u0000'
                        inputConnection.commitText(makeHan().toString(), 1)
                    }
                    else{
                        temp = doubleJonFlag
                        jon = jonFlag
                        inputConnection.commitText(makeHan().toString(), 1)
                    }
                    state = 2
                    clear()
                    cho = temp
                    jun = c
                    inputConnection.setComposingText(makeHan().toString(), 1)
                }
            }
        }
    }

    fun commitSpace(){
        directlyCommit()
        inputConnection.commitText(" ", 1)
    }

    open fun directlyCommit(){
        if(state == 0){
            return
        }
        inputConnection.commitText(makeHan().toString(), 1)
        state = 0
        clear()
    }

    open fun delete(){
        when(state){
            0 -> {
                inputConnection.deleteSurroundingText(1, 0)
            }
            1 -> {
                cho = '\u0000'
                state = 0
                inputConnection.setComposingText("", 1)
                inputConnection.commitText("",1)
            }
            2 -> {
                if(junFlag != '\u0000'){
                    jun = junFlag
                    junFlag = '\u0000'
                    state = 2
                    inputConnection.setComposingText(makeHan().toString(), 1)
                }
                else{
                    jun = '\u0000'
                    junFlag = '\u0000'
                    state = 1
                    inputConnection.setComposingText(cho.toString(), 1)
                }
            }
            3 -> {
                if(doubleJonFlag == '\u0000'){
                    jon = '\u0000'
                    state = 2
                }
                else{
                    jon = jonFlag
                    jonFlag = '\u0000'
                    doubleJonFlag = '\u0000'
                    state = 3
                }
                inputConnection.setComposingText(makeHan().toString(), 1)
            }
        }
    }

    fun doubleJunEnable(c:Char):Boolean{
        when(jun){
            'ㅗ' -> {
                if(c == 'ㅏ'){
                    junFlag = jun
                    jun = 'ㅘ'
                    return true
                }
                if(c == 'ㅐ'){
                    junFlag = jun
                    jun = 'ㅙ'
                    return true
                }
                if(c == 'ㅣ'){
                    junFlag = jun
                    jun = 'ㅚ'
                    return true
                }
                return false
            }
            'ㅜ' -> {
                if(c == 'ㅓ'){
                    junFlag = jun
                    jun = 'ㅝ'
                    return true
                }
                if(c == 'ㅔ'){
                    junFlag = jun
                    jun = 'ㅞ'
                    return true
                }
                if(c == 'ㅣ'){
                    junFlag = jun
                    jun = 'ㅟ'
                    return true
                }
                return false
            }
            'ㅡ' -> {
                if(c == 'ㅣ'){
                    junFlag = jun
                    jun = 'ㅢ'
                    return true
                }
                return false
            }
            else -> {
                return false
            }
        }
    }

    fun doubleJonEnable(c:Char):Boolean{
        jonFlag = jon
        doubleJonFlag = c
        when(jon){
            'ㄱ' -> {
                if(c == 'ㅅ'){
                    jon = 'ㄳ'
                    return true
                }
                return false
            }
            'ㄴ' -> {
                if(c == 'ㅈ'){
                    jon = 'ㄵ'
                    return true
                }
                if(c == 'ㅎ'){
                    jon = 'ㄶ'
                    return true
                }
                return false
            }
            'ㄹ' -> {
                if(c == 'ㄱ'){
                    jon = 'ㄺ'
                    return true
                }
                if(c == 'ㅁ'){
                    jon = 'ㄻ'
                    return true
                }
                if(c == 'ㅂ'){
                    jon = 'ㄼ'
                    return true
                }
                if(c == 'ㅅ'){
                    jon = 'ㄽ'
                    return true
                }
                if(c == 'ㅌ'){
                    jon = 'ㄾ'
                    return true
                }
                if(c == 'ㅍ'){
                    jon = 'ㄿ'
                    return true
                }
                if(c == 'ㅎ'){
                    jon = 'ㅀ'
                    return true
                }
                return false
            }
            'ㅂ' -> {
                if(c == 'ㅅ'){
                    jon = 'ㅄ'
                    return true
                }
                return false
            }
            else -> {
                return false
            }
        }
    }
    fun junAvailable():Boolean{
        if(jun == 'ㅙ' || jun == 'ㅞ' || jun == 'ㅢ'|| jun == 'ㅐ' || jun == 'ㅔ' || jun == 'ㅛ' || jun == 'ㅒ' || jun == 'ㅖ'){
            return false
        }
        return true
    }

    fun isDoubleJun():Boolean{
        if(jun == 'ㅙ' || jun == 'ㅞ' || jun == 'ㅚ'|| jun == 'ㅝ' || jun == 'ㅟ' || jun == 'ㅘ' || jun == 'ㅢ'){
            return true
        }
        return false
    }
}