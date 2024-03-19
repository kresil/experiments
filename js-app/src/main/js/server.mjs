`use strict`

import {Adapter} from 'kresil-experiments-kmp'
import express from 'express'

const app = express()
const port = 8080
const adapterInstance = Adapter.getInstance();

app.get('/add', (req, res) => {
    adapterInstance.addBooks()
    res.send('Hardcoded books added successfully')
})

app.get('/books', (req, res) => {
    const books = adapterInstance.getBooks()
    res.send(books)
})

// Route to handle clearing all books via DELETE request
app.get('/clear', (req, res) => {
    adapterInstance.clearBooks()
    res.send('All books cleared successfully');
});

app.get('/platform', (req, res) => {
    const platform = adapterInstance.getPlatformType()
    res.send(platform)
})

// will never succeed
app.get('/ignore', (req, res) => {
    const result = adapterInstance.ignored()
    res.send(result)
})

// Start the server
app.listen(port, () => {
    console.log(`Server is listening at http://localhost:${port}`)
})
