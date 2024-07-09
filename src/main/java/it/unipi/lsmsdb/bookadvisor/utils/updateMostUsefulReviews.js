// Create indexes to optimize query performance
// db.reviews.createIndex({ "book_id": 1 });
// db.reviews.createIndex({ "count_up_votes": 1 });
// db.reviews.createIndex({ "count_down_votes": 1 });

exports = async function() {
  const db = context.services.get("mongodb-atlas").db("yourDatabase");
  const booksCollection = db.collection("books");
  const reviewsCollection = db.collection("reviews");

  const booksCursor = booksCollection.find();
  
  while (await booksCursor.hasNext()) {
    const book = await booksCursor.next();
    const mostUsefulReviews = await reviewsCollection.aggregate([
      { "$match": { "book_id": book._id } },
      { "$addFields": { "helpfulness": { "$subtract": ["$count_up_votes", "$count_down_votes"] } } },
      { "$sort": { "helpfulness": -1 } },
      { "$limit": 10 },
      { "$project": { "helpfulness": 0 } } // Exclude the 'helpfulness' field
    ]).toArray();

    await booksCollection.updateOne(
      { "_id": book._id },
      { "$set": { "most_10_useful_reviews": mostUsefulReviews } }
    );
  }
};
