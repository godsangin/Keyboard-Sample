package com.myhome.rpgkeyboard.keyboardview

import android.util.Log
import android.view.inputmethod.InputConnection

class ChunjiinMaker(val inputConnection: InputConnection): HangulMaker(inputConnection){
    var testChar:Char = '\u0000'
    var isComposingMoum = false
    var onlyMoum = false
    var gkList = listOf<Char>('ㄱ','ㅋ','ㄲ')
    var nlList = listOf<Char>('ㄴ','ㄹ')
    var dtList = listOf<Char>('ㄷ','ㅌ','ㄸ')
    var bpList = listOf<Char>('ㅂ', 'ㅍ', 'ㅃ')
    var shList = listOf<Char>('ㅅ', 'ㅎ', 'ㅆ')
    var jchList = listOf<Char>('ㅈ','ㅊ','ㅉ')
    var aiueomList = listOf<Char>('ㅇ','ㅁ')
    val commonKeywords = listOf<String>(".",",","?","!")
    var wholeList:List<List<Char>> = listOf(gkList, nlList, dtList, bpList, shList, jchList, aiueomList)
    var myList:List<Char>? = null
    var listIndex = 0
    var junFlagChunjiin = '\u0000'
    var keywordIndex = 0
    var keywordExpect = false
    var stateThreeDot = false
    override fun commit(c:Char){
        if(keywordExpect){
            inputConnection.finishComposingText()
            keywordExpect = false
        }
        if(c == 'ㅣ' || c == '·' || c == 'ㅡ'){//모음구성
            if(super.state == 0){//모음만으로 구성된 글자 ex) ㅠㅠㅠㅠㅠㅠㅠ
                onlyMoum = true
                if(testChar == '\u0000'){
                    testChar = c
                    inputConnection.setComposingText(testChar.toString(), 1)
                }
                else if(combination(c)){
                    inputConnection.setComposingText(testChar.toString(), 1)
                }
                else{
                    inputConnection.commitText(testChar.toString(), 1)
                    testChar = c
                    inputConnection.setComposingText(testChar.toString(), 1)
                }
                junFlagChunjiin = '\u0000'
            }
            else if(!isComposingMoum){
                onlyMoum = false
                testChar = c
                if(c == '·'){
                    if(super.state == 3){
                        //종성까지 추가된 상태
                        inputConnection.setComposingText(super.makeHan().toString() + testChar, 2)
                        stateThreeDot = true
                    }
                    else if(!super.junAvailable()){//더이상 추가될 수 없는 모음일 경우 ex) 왜 + ·
                        super.directlyCommit()
                        inputConnection.setComposingText(testChar.toString(), 1)
                    }
                    else{
                        inputConnection.setComposingText(super.makeHan().toString() + testChar, 2)
                        super.state = 2
                    }
                }
                else{
                    super.commit(testChar)
                }
                listIndex = 0
                isComposingMoum = true
            }
            else{
                if(combination(c)){//이중모음으로 선언 가능한 경우
                    if(testChar == '‥'){
                        inputConnection.setComposingText(super.makeHan().toString() + testChar, 2)
                    }
                    else if(super.state == 2){//모음이 기대되는 상태
                        if(super.isDoubleJun()){//이중모음인 경우 두 모음을 모두 지운다.
                            super.delete()
                            super.delete()
                        }
                        else{//이중모음이 아닌 경우
                            super.delete()
                        }
                        super.commit(testChar)
                        super.junFlag = junFlagChunjiin//이중 모음일 경우 이전 모음을 설정한다.
                        junFlagChunjiin = '\u0000'//초기화
                    }
                    else{
                        super.commit(testChar)
//                        isComposingMoum = false
                    }
                }
                else{//이 + ㅣ와 같은 경우 이전 글자를 commit하고 모음으로만 구성된다고 설정한다.
                    super.directlyCommit()
                    testChar = c
                    inputConnection.setComposingText(testChar.toString(), 1)
                    isComposingMoum = false
                    onlyMoum = true
                }
            }

        }
        else if(myList == null){//첫입력
            if(onlyMoum){//모음으로만 구성된 문자를 작성중이었다면 commit한다.
                inputConnection.commitText(testChar.toString(), 1)
            }
            onlyMoum = false
            testChar = c
            for(list in wholeList){//전체 리스트를 순회하며 현재 텍스트를 포함하는 리스트를 찾는다.
                if(list.indexOf(testChar) >= 0){
                    myList = list
                    listIndex = 1
                }
            }
            super.commit(testChar)
            isComposingMoum = false
        }
        else if(myList?.indexOf(c)!! >= 0){//현재 작성중인 문자에서 파생될수 있는 문자를 출력 ex) ㄱ -> ㅋ
            if(onlyMoum){
                inputConnection.commitText(testChar.toString(), 1)
            }
            onlyMoum = false
            if(listIndex == myList?.size){//더이상 파생할 수 없을 경우 첫번째 문자로 돌아간다. ex) ㄲ -> ㄱ
                listIndex = 0
            }
            testChar = myList?.get(listIndex)!!
            listIndex++
            if(super.state == 1 || super.state == 3){//단어를 대체하기 위하여 delete한 뒤 새로운 문자를 commit한다.
                super.delete()
                super.commit(testChar)
            }
            else{
                super.commit(testChar)
            }
            isComposingMoum = false
        }
        else{
            onlyMoum = false
            testChar = c
            for(list in wholeList){
                if(list.indexOf(testChar) >= 0){
                    myList = list
                    listIndex = 1
                }
            }
            super.commit(c)
            isComposingMoum = false
        }
    }

    fun combination(c:Char):Boolean{//모음을 구성하기 위한 조합 성공시 true리턴
        when(testChar){
            'ㅣ' -> {
                if(c == '·'){
                    testChar = 'ㅏ'
                    return true
                }
                else{
                    return false
                }
            }
            '·' -> {
                if(c == 'ㅡ'){
                    testChar = 'ㅗ'
                    return true
                }
                else if(c == 'ㅣ'){
                    testChar = 'ㅓ'
                    return true
                }
                else if(c == '·'){
                    testChar = '‥'
                    return true
                }
                else{
                    return false
                }
            }
            '‥' -> {
                if(c == 'ㅣ'){
                    testChar = 'ㅕ'
                    return true
                }
                else if(c == 'ㅡ'){
                    testChar = 'ㅛ'
                    return true
                }
                else{
                    return false
                }
            }
            'ㅡ' -> {
                if(c == 'ㅣ'){
                    testChar = 'ㅢ'
                    junFlagChunjiin = 'ㅡ'
                    return true
                }
                else if(c == '·'){
                    testChar = 'ㅜ'
                    return true
                }
                else{
                    return false
                }
            }
            'ㅏ' -> {
                if(c == '·'){
                    testChar = 'ㅑ'
                    return true
                }
                else if(c == 'ㅣ'){
                    testChar = 'ㅐ'
                    return true
                }
                else {
                    return false
                }
            }
            'ㅓ' -> {
                if(c == 'ㅣ'){
                    testChar = 'ㅔ'
                    return true
                }
                else{
                    return false
                }
            }
            'ㅑ' -> {
                if(c == 'ㅣ'){
                    testChar = 'ㅒ'
                    return true
                }
                else{
                    return false
                }
            }
            'ㅕ' -> {
                if(c == 'ㅣ'){
                    testChar = 'ㅖ'
                    return true
                }
                else{
                    return false
                }
            }
            'ㅜ' -> {
                if(c == '·'){
                    testChar = 'ㅠ'
                    return true
                }
                if(c == 'ㅣ'){
                    testChar = 'ㅟ'
                    junFlagChunjiin = 'ㅜ'
                    return true
                }
                else{
                    return false
                }
            }
            'ㅠ' -> {
                if(c == 'ㅣ'){
                    testChar = 'ㅝ'
                    junFlagChunjiin = 'ㅜ'
                    return true
                }
                else{
                    return false
                }
            }
            'ㅗ' -> {
                if(c == 'ㅣ'){
                    testChar = 'ㅚ'
                    junFlagChunjiin = 'ㅗ'
                    return true
                }
                else{
                    return false
                }
            }
            'ㅚ' -> {
                if(c == '·'){
                    testChar ='ㅘ'
                    junFlagChunjiin = 'ㅗ'
                    return true
                }
                return false
            }
            'ㅘ' -> {
                if(c == 'ㅣ'){
                    testChar = 'ㅙ'
                    junFlagChunjiin = 'ㅗ'
                    return true
                }
                return false
            }
            'ㅝ' -> {
                if(c == 'ㅣ'){
                    testChar = 'ㅞ'
                    junFlagChunjiin = 'ㅜ'
                    return true
                }
                return false
            }
            else -> {
                return false
            }
        }
    }

    override fun directlyCommit(){
        super.directlyCommit()
        inputConnection.finishComposingText()
        clearChunjiin()
    }

    override fun delete(){
        if(onlyMoum){//현재 커서가 모음으로만 구성된 문자일 경우
            inputConnection.setComposingText("", 1)
            clearChunjiin()
        }
        else if(stateThreeDot){//HangulMaker의 3번상태(자음+모음+자음)상태에서 .기호가 들어와 있는 상태
            inputConnection.setComposingText(super.makeHan().toString(), 1)
            clearChunjiin()
            stateThreeDot = false
        }
        else if(super.state == 2 && super.isDoubleJun()){//상태 2이면서 이중모음이 들어와 있는 상태
            super.delete()
            setTestCharBefore()
        }
        else {
            clearChunjiin()
            super.delete()
        }
        listIndex = 0
    }

    fun commonKeywordCommit(){//특수문자 입력 시
        directlyCommit()
        if(keywordIndex == commonKeywords.size){
            keywordIndex = 0
        }
        inputConnection.setComposingText(commonKeywords[keywordIndex++], 1)
        keywordExpect = true
    }

    fun clearChunjiin(){
        testChar = '\u0000'
        isComposingMoum = false
        myList = null
        listIndex = 0
        onlyMoum = false
    }

    fun isEmpty():Boolean{
        if(super.state == 0 && testChar == '\u0000'){
            return true
        }
        return false
    }
    fun setTestCharBefore(){//이중모음 이전의 상태를 반환
        if(testChar == 'ㅚ' || testChar == 'ㅘ' || testChar == 'ㅙ'){
            testChar = 'ㅗ'
        }
        else if(testChar == 'ㅟ' || testChar == 'ㅝ' || testChar == 'ㅞ'){
            testChar = 'ㅜ'
        }
        else if(testChar == 'ㅢ'){
            testChar = 'ㅡ'
        }
    }
}