-- insert initial test data
-- the IDs are hardcoded to enable references between further test data
-- negative IDs are used to not interfere with user-entered data and allow clean deletion of test data

-- clear all references to have no issues when deleting
UPDATE horse
SET mother_id=null,
    father_id=null,
    owner_id=null
WHERE id < 0;

DELETE FROM owner where id < 0;
INSERT INTO owner (id, first_name, last_name, email) VALUES
(-1, 'Bob', 'Jones', 'bob.jones@example.com'),
(-2, 'Alice', 'Lee', 'alice.lee@example.com'),
(-3, 'Tom', 'Smith', 'tom.smith@example.com'),
(-4, 'Maggie', 'Nguyen', 'maggie.nguyen@example.com'),
(-5, 'Jim', 'Brown', 'jim.brown@example.com'),
(-6, 'Emily', 'Kim', 'emily.kim@example.com'),
(-7, 'Joshua', 'Wong', 'joshua.wong@example.com'),
(-8, 'Sarah', 'Chen', 'sarah.chen@example.com'),
(-9, 'Jack', 'Lee', 'jack.lee@example.com'),
(-10, 'Karen', 'Kim', 'karen.kim@example.com');

DELETE FROM horse where id < 0;
INSERT INTO horse (id, name, description, date_of_birth, sex, owner_id, mother_id, father_id) VALUES
(-1, 'Thunder', 'Beautiful black stallion with a white blaze', '2018-05-15', 'MALE', -1, NULL, NULL),
(-2, 'Daisy', 'Sweet mare with a chestnut coat and white socks', '2017-03-10', 'FEMALE', NULL, NULL, NULL),
(-3, 'Spirit', 'Gentle grey stallion with a long mane', '2015-11-22', 'MALE', -3, NULL, NULL),
(-4, 'Bella', 'Elegant mare with a dappled grey coat', '2019-02-01', 'FEMALE', -4, -2, -1),
(-5, 'Max', 'Playful colt with a chestnut coat and a white star', '2020-09-05', 'MALE', NULL, NULL, -3),
(-6, 'Luna', 'Curious filly with a black coat and white stockings', '2021-02-18', 'FEMALE', -6, -4, -3),
(-7, 'Romeo', 'Handsome stallion with a bay coat and a white blaze', '2016-08-11', 'MALE', -7, NULL, NULL),
(-8, 'Sophie', 'Gentle mare with a chestnut coat and a white blaze', '2014-06-02', 'FEMALE', NULL, NULL, NULL),
(-9, 'Apollo', 'Powerful stallion with a black coat and a muscular build', '2017-09-19', 'MALE', -9, NULL, NULL),
(-10, 'Misty', 'Shy mare with a palomino coat and a long mane', '2018-12-07', 'FEMALE', -10, -8, -7);
