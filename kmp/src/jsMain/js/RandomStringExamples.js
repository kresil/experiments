const randomStringExamples = require("randomstring.kt");

// Examples:
// Generate a random string (default length: 32)
const randomDefault = randomStringExamples.generate();
console.log("Random default:", randomDefault);

// Generate a random string with a specific length (e.g., 7 characters)
const randomLength7 = randomStringExamples.generate(7);
console.log("Random length 7:", randomLength7);

// Generate a random string with custom options (e.g., alphabetic characters only)
const randomAlphabetic = randomStringExamples.generate({ length: 12, charset: "alphabetic" });
console.log("Random alphabetic:", randomAlphabetic);

// export for use in Kotlin
module.exports.randomstring = randomStringExamples;