// Certainly! Using MongoDB Atlas's Scheduled Triggers is a powerful way to automate tasks directly in the cloud. Here’s a step-by-step guide on how to set up and use Scheduled Triggers in MongoDB Atlas.

// ### Step-by-Step Guide to Using MongoDB Atlas Scheduled Triggers

// #### 1. **Set Up MongoDB Atlas**

// If you haven’t already, sign up for MongoDB Atlas and create a cluster:
// - [Sign up for MongoDB Atlas](https://www.mongodb.com/cloud/atlas/register)

// #### 2. **Access MongoDB Atlas App Services**

// 1. **Navigate to your cluster in MongoDB Atlas.**
// 2. **Click on the `App Services` tab in the left-hand sidebar.**
// 3. **Click `Create New App`** and follow the prompts to set up your app.

// #### 3. **Create a Scheduled Trigger**

// 1. **Within your App Services, navigate to `Triggers` in the left-hand menu.**
// 2. **Click on `Add Trigger`.**
// 3. **Select `Scheduled Trigger` and provide the necessary details:**
//    - **Name:** Provide a name for your trigger.
//    - **Schedule:** Define when and how often your trigger should run (e.g., every day at midnight).

// #### 4. **Define the Trigger Function**

// 1. **In the Function section, click on `New Function`.**
// 2. **Name your function** (e.g., `updateMostUsefulReviews`).
// 3. **Add the following code** in the function editor to implement the logic for updating the most useful reviews:

// updateReviews.js

const { MongoClient } = require('mongodb');

// Replace the following with your MongoDB Atlas connection string
const uri = "mongodb://10.1.1.20:27020,10.1.1.21:27020,10.1.1.23:27020/";

async function updateMostUsefulReviews() {
  const client = new MongoClient(uri, { useNewUrlParser: true, useUnifiedTopology: true });

  try {
    await client.connect();
    const db = client.db("BookAdvisor");
    const booksCollection = db.collection("books");
    const reviewsCollection = db.collection("reviews");

    const booksCursor = booksCollection.find();

    while (await booksCursor.hasNext()) {
      const book = await booksCursor.next();

      // Fetch reviews directly using review_ids from the book document
      const reviewIds = book.review_ids.map(id => ObjectId(id)); // Convert review_ids to ObjectId
  
      const mostUsefulReviews = await reviewsCollection.aggregate([
        { $match: { _id: { $in: reviewIds } } }, // Match reviews by their IDs
        { $addFields: {
            "helpfulness": { $subtract: ["$count_up_votes", "$count_down_votes"] }
          }
        },
        { $sort: { "helpfulness": -1 } },
        { $limit: 10 },
        { $project: { "helpfulness": 0 } } // Exclude helpfulness field from final output
      ]).toArray();

      // Update the book document with the most useful reviews
      await booksCollection.updateOne(
        { "_id": book._id },
        { "$set": { "most_10_useful_reviews": mostUsefulReviews } }
      );
    }
  } finally {
    await client.close();
  }
}

updateMostUsefulReviews().catch(console.error);

// 4. **Save your function.**

// #### 5. **Configure Authentication and Roles (Optional)**

// Depending on your data access needs and security considerations, you might need to configure authentication and roles to ensure that the function has the necessary permissions to read from and write to your collections.

// 1. **Navigate to `Rules` under the `Data` section.**
// 2. **Configure the rules** to allow the function to access the `books` and `reviews` collections.

// #### 6. **Deploy Your App**

// 1. **Click `Deploy` in the upper right corner** of the App Services dashboard.
// 2. **Follow the prompts** to deploy your changes.

// ### Summary

// By following these steps, you can set up a Scheduled Trigger in MongoDB Atlas to periodically update the most useful reviews for each book in your collection. This is a convenient and scalable way to automate backend processes directly within MongoDB's managed cloud environment.

// If you encounter any issues or need more advanced configurations, refer to the [MongoDB Atlas App Services documentation](https://www.mongodb.com/docs/atlas/app-services/) for detailed guidance and troubleshooting tips.
