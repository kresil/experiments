package models

/**
 * A simple book model.
 * Needs to be marked expect because of js export
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING", "NO_ACTUAL_FOR_EXPECT")
expect class Book(title: String, author: String)