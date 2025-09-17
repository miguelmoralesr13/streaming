// MongoDB initialization script

// Switch to video_streaming database
db = db.getSiblingDB('video_streaming');

// Also create development database
db = db.getSiblingDB('video_streaming_dev');

// Create a user for the application in both databases
db.createUser({
    user: 'appuser',
    pwd: 'apppassword',
    roles: [
        { role: 'readWrite', db: 'video_streaming' },
        { role: 'readWrite', db: 'video_streaming_dev' }
    ]
});

// Create collections
db.createCollection('users');
db.createCollection('videos');
db.createCollection('video_metadata');

// Create indexes for better performance
db.users.createIndex({ "username": 1 }, { unique: true });
db.users.createIndex({ "email": 1 }, { unique: true });
db.videos.createIndex({ "uploadedBy": 1 });
db.videos.createIndex({ "isPublic": 1 });
db.videos.createIndex({ "status": 1 });
db.videos.createIndex({ "createdAt": -1 });
db.video_metadata.createIndex({ "videoId": 1 }, { unique: true });

// Create admin user
db.users.insertOne({
    username: "admin",
    email: "admin@videostreaming.com",
    password: "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi", // password: admin123
    roles: ["ADMIN", "USER"],
    enabled: true,
    accountNonExpired: true,
    accountNonLocked: true,
    credentialsNonExpired: true,
    createdAt: new Date(),
    updatedAt: new Date()
});

print("MongoDB initialization completed successfully!");
