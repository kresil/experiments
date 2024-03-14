import {Person} from '../../../../build/js/packages/kresil-experiments-js-app/kotlin/kresil-experiments-js-app.mjs';

// Create an instance of Person
const john = new Person('John');

// Call methods on the Person instance
john.hello();
john.helloWithGreeting('Greetings');
john.useConsole();
john.toJson();
john.accessJsonProps();
