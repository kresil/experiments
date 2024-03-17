package models

/**
 * A simple book model.
 * Needs to be marked expect because of js export
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class Book(title: String, author: String)