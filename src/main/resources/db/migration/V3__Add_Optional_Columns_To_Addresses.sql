ALTER TABLE addresses
    ADD COLUMN apartment_number VARCHAR(50),
    ADD COLUMN company VARCHAR(100),
    ADD COLUMN phone VARCHAR(20),
    ADD COLUMN additional_info VARCHAR(500);