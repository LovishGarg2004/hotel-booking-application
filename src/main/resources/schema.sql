CREATE TABLE users (
    user_id UUID PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(100),
    dob DATE,
    phone VARCHAR(20),
    role VARCHAR(30),
    created_at TIMESTAMP,
    email_verified BOOLEAN

);

CREATE TABLE amenity (
    amenity_id UUID PRIMARY KEY,
    name VARCHAR(100),
    description VARCHAR(255),
    image_url VARCHAR(255)
);

CREATE TABLE hotel (
    hotel_id UUID PRIMARY KEY,
    owner_id UUID,
    name VARCHAR(100),
    description CLOB,
    address CLOB,
    city VARCHAR(50),
    state VARCHAR(50),
    country VARCHAR(50),
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    created_at TIMESTAMP,
    is_approved BOOLEAN,
    FOREIGN KEY (owner_id) REFERENCES users(user_id)
);

CREATE TABLE room (
    room_id UUID PRIMARY KEY,
    hotel_id UUID,
    room_type VARCHAR(30),
    capacity INT,
    base_price DECIMAL(10,2),
    total_rooms INT,
    FOREIGN KEY (hotel_id) REFERENCES hotel(hotel_id)
);

CREATE TABLE room_amenity (
    id UUID PRIMARY KEY,
    room_id UUID,
    amenity_id UUID,
    FOREIGN KEY (room_id) REFERENCES room(room_id),
    FOREIGN KEY (amenity_id) REFERENCES amenity(amenity_id)
);

CREATE TABLE room_availability (
    availability_id UUID PRIMARY KEY,
    room_id UUID,
    date DATE,
    available_rooms INT,
    FOREIGN KEY (room_id) REFERENCES room(room_id)
);

CREATE TABLE booking (
    booking_id UUID PRIMARY KEY,
    user_id UUID,
    room_id UUID,
    check_in DATE,
    check_out DATE,
    guests INT,
    final_price DECIMAL(10,2),
    status VARCHAR(30),
    created_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (room_id) REFERENCES room(room_id)
);

CREATE TABLE payment (
    payment_id UUID PRIMARY KEY,
    booking_id UUID,
    user_id UUID,
    amount DECIMAL(10,2),
    payment_date TIMESTAMP,
    method VARCHAR(30),
    status VARCHAR(30),
    FOREIGN KEY (booking_id) REFERENCES booking(booking_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE pricing_rule (
    rule_id UUID PRIMARY KEY,
    hotel_id UUID,
    rule_type VARCHAR(30),
    rule_value CLOB,
    start_date DATE,
    end_date DATE,
    FOREIGN KEY (hotel_id) REFERENCES hotel(hotel_id)
);

CREATE TABLE review (
    review_id UUID PRIMARY KEY,
    user_id UUID,
    hotel_id UUID,
    rating INT,
    comment CLOB,
    created_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (hotel_id) REFERENCES hotel(hotel_id)
);

CREATE TABLE image (
    image_id UUID PRIMARY KEY,
    type VARCHAR(30),
    reference_id UUID,
    image_url CLOB,
    uploaded_at TIMESTAMP
); 