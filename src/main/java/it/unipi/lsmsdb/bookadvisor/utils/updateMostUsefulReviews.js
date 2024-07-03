
// // Create indexes to optimize query performance
// db.reviews.createIndex({ "book_id": 1 });
// db.reviews.createIndex({ "count_up_votes": 1 });
// db.reviews.createIndex({ "count_down_votes": 1 });

const { MongoClient } = require('mongodb');
const cron = require('node-cron');

// Connection URI
const uri = "mongodb://localhost:27017"; 

async function updateMostUsefulReviews() {
    const client = new MongoClient(uri, { useNewUrlParser: true, useUnifiedTopology: true });
    try {
        await client.connect();
        const db = client.db("BookAdvisor"); 

        const books = db.collection('books');
        const reviews = db.collection('reviews');

        const bookCursor = books.find();
        while (await bookCursor.hasNext()) {
            const book = await bookCursor.next();
            const mostUsefulReviews = await reviews.aggregate([
                { "$match": { "book_id": book._id } },
                { "$addFields": { "helpfulness": { "$subtract": ["$count_up_votes", "$count_down_votes"] } } },
                { "$sort": { "helpfulness": -1 } },
                { "$limit": 10 }
            ]).toArray();

            await books.updateOne(
                { "_id": book._id },
                { "$set": { "most_10_useful_reviews": mostUsefulReviews } }
            );
        }
    } catch (err) {
        console.error(err);
    } finally {
        await client.close();
    }
}

// Schedule the task to run once a day at midnight
cron.schedule('0 0 * * *', () => {
    console.log('Running the updateMostUsefulReviews script');
    updateMostUsefulReviews();
});

// Run the script immediately
updateMostUsefulReviews();
