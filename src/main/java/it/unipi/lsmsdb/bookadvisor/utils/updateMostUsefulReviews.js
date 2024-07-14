const { MongoClient, ObjectId } = require('mongodb');
const uri = "your_mongodb_connection_string";

async function updateReviews() {
    const client = new MongoClient(uri, { useNewUrlParser: true, useUnifiedTopology: true });

    try {
        await client.connect();
        const database = client.db('BookAdvisor');
        const booksCollection = database.collection('books');
        const reviewsCollection = database.collection('reviews');

        const booksCursor = booksCollection.find();

        while (await booksCursor.hasNext()) {
            const book = await booksCursor.next();
            const reviewIds = book.review_ids.map(id => ObjectId(id));

            const mostUsefulReviews = await reviewsCollection.aggregate([
                { $match: { _id: { $in: reviewIds } } },
                { $addFields: { "helpfulness": { $subtract: ["$count_up_votes", "$count_down_votes"] } } },
                { $sort: { "helpfulness": -1 } },
                { $limit: 10 },
                { $project: { "helpfulness": 0 } }
            ]).toArray();

            await booksCollection.updateOne(
                { "_id": book._id },
                { "$set": { "most_10_useful_reviews": mostUsefulReviews } }
            );
        }
    } finally {
        await client.close();
    }
}

updateReviews().catch(console.error);
