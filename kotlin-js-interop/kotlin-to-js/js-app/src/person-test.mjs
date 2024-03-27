import {Person} from 'k2j-kotlin-app/kotlin/k2j-kotlin-app.mjs' 
import {assert} from 'chai' 
import {describe} from 'mocha' 

describe('Imported Person Class Test', function () {
    const john = new Person('John') 
    // Test cases
    it('should say hello', function () {
        assert.strictEqual(john.hello(), 'Hello John!') 
    }) 

    it('should say hello with greeting', function () {
        assert.strictEqual(john.helloWithGreeting('Greetings'), 'Greetings John!') 
    }) 

    it('should use console', function () {
        john.useConsole() 
    }) 

    it('should convert to JSON', function () {
        const expectedJson = JSON.stringify({name: 'John', age: 42}) 
        assert.strictEqual(john.toJson(), expectedJson) 
    }) 

    it('should access JSON properties', function () {
        const json = JSON.parse(john.toJson()) 
        assert.strictEqual(json.name, 'John') 
        assert.strictEqual(json.age, 42) 
        assert.strictEqual(json.toLocaleString(), '[object Object]')
        assert.strictEqual(json.unknown, undefined) 
    }) 
}) 
