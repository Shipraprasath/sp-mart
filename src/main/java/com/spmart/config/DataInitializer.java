package com.spmart.config;

import com.spmart.entity.*;
import com.spmart.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final CustomerOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           SellerProfileRepository sellerProfileRepository,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           AddressRepository addressRepository,
                           CustomerOrderRepository orderRepository,
                           OrderItemRepository orderItemRepository,
                           OrderStatusHistoryRepository historyRepository,
                           PaymentRepository paymentRepository,
                           ReviewRepository reviewRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.historyRepository = historyRepository;
        this.paymentRepository = paymentRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0 && categoryRepository.count() >= 12) {
            log.info("SP-Mart database already seeded with 12 electronics categories. Skipping data initialization.");
            return;
        }

        log.info("Initializing SP-Mart premium electronics marketplace seed data...");

        // If partially seeded from earlier version without 12 categories, clean up to ensure fresh consistency
        if (userRepository.count() > 0 && categoryRepository.count() < 12) {
            log.info("Upgrading existing database to 12 Electronics categories...");
            orderItemRepository.deleteAll();
            historyRepository.deleteAll();
            paymentRepository.deleteAll();
            orderRepository.deleteAll();
            reviewRepository.deleteAll();
            productRepository.deleteAll();
            categoryRepository.deleteAll();
            sellerProfileRepository.deleteAll();
            userRepository.deleteAll();
        }

        // 1. Seed Administrators, Sellers, and Buyers
        User admin = new User(
                "SP-Mart Administrator",
                "admin@spmart.com",
                passwordEncoder.encode("Admin@123"),
                "9876543210",
                Role.ROLE_ADMIN
        );
        admin = userRepository.save(admin);

        User seller1 = new User(
                "Apex Electronics",
                "seller@spmart.com",
                passwordEncoder.encode("Seller@123"),
                "9811002233",
                Role.ROLE_SELLER
        );
        seller1 = userRepository.save(seller1);

        SellerProfile profile1 = new SellerProfile(
                seller1,
                "Apex Tech Official",
                "seller@spmart.com",
                "9811002233",
                "29AAAAA0000A1Z5",
                "Plot 42, Electronic City Phase 1, Bengaluru, Karnataka 560100",
                "Authorized retailer of flagship smartphones, gaming laptops, and premium audio equipment.",
                SellerStatus.APPROVED
        );
        profile1.setRating(4.9);
        sellerProfileRepository.save(profile1);

        User seller2 = new User(
                "DigiTech Solutions",
                "digitech@spmart.com",
                passwordEncoder.encode("Seller@123"),
                "9822334455",
                Role.ROLE_SELLER
        );
        seller2 = userRepository.save(seller2);

        SellerProfile profile2 = new SellerProfile(
                seller2,
                "DigiTech Home Appliances",
                "digitech@spmart.com",
                "9822334455",
                "29BBBBB1111B2Z6",
                "Industrial Area Phase 2, Whitefield, Bengaluru, Karnataka 560066",
                "Specialist in energy-efficient smart appliances, 4K TVs, and smart home automation devices.",
                SellerStatus.APPROVED
        );
        profile2.setRating(4.8);
        sellerProfileRepository.save(profile2);

        User pendingSeller = new User(
                "FutureGadgets Inc",
                "pending.seller@spmart.com",
                passwordEncoder.encode("Seller@123"),
                "9833445566",
                Role.ROLE_SELLER
        );
        pendingSeller = userRepository.save(pendingSeller);

        SellerProfile pendingProfile = new SellerProfile(
                pendingSeller,
                "FutureGadgets Hub",
                "pending.seller@spmart.com",
                "9833445566",
                "29CCCCC2222C3Z7",
                "Sector 62, Noida, Uttar Pradesh 201309",
                "Distributor of wearable electronics and computer accessories awaiting verification.",
                SellerStatus.PENDING_APPROVAL
        );
        sellerProfileRepository.save(pendingProfile);

        User buyer = new User(
                "Rahul Verma",
                "buyer@spmart.com",
                passwordEncoder.encode("Buyer@123"),
                "9123456789",
                Role.ROLE_CUSTOMER
        );
        buyer = userRepository.save(buyer);

        User customer = new User(
                "Priya Sharma",
                "customer@spmart.com",
                passwordEncoder.encode("Customer@123"),
                "9811223344",
                Role.ROLE_CUSTOMER
        );
        customer = userRepository.save(customer);

        // 2. Seed Addresses for Buyers
        Address addr1 = new Address(
                buyer,
                "Rahul Verma",
                "9123456789",
                "Flat 402, Skyline Residency",
                "Outer Ring Road, Marathahalli",
                "Marathahalli",
                "Bengaluru",
                "Karnataka",
                "560037",
                "Near Innovative Multiplex",
                true
        );
        addressRepository.save(addr1);

        Address addr2 = new Address(
                buyer,
                "Rahul Verma (Office)",
                "9123456789",
                "Tower B, Tech Park Campus",
                "Whitefield Main Road",
                "Whitefield",
                "Bengaluru",
                "Karnataka",
                "560066",
                "Opposite Metro Station",
                false
        );
        addressRepository.save(addr2);

        Address addr3 = new Address(
                customer,
                "Priya Sharma",
                "9811223344",
                "House #84, Sector 14",
                "DLF Phase 2",
                "DLF",
                "Gurugram",
                "Haryana",
                "122002",
                "Near Cyber Hub",
                true
        );
        addressRepository.save(addr3);

        // 3. Seed 12 Specific Electronics Categories
        Category catPhones = categoryRepository.save(new Category(
                "Smartphones", "smartphones",
                "Next-generation 5G flagship smartphones, foldable devices, and performance powerhouses.",
                "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80",
                "smartphone", 1));

        Category catLaptops = categoryRepository.save(new Category(
                "Laptops & Computers", "laptops-computers",
                "High-performance creator workstations, sleek ultrabooks, and extreme gaming rigs.",
                "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&q=80",
                "laptop", 2));

        Category catTvs = categoryRepository.save(new Category(
                "Televisions", "televisions",
                "Cinematic 4K OLED, QLED, and Mini-LED Smart TVs with Dolby Vision and Atmos.",
                "https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=800&q=80",
                "tv", 3));

        Category catAcs = categoryRepository.save(new Category(
                "Air Conditioners", "air-conditioners",
                "AI-powered convertible inverter split and window ACs with 5-star energy ratings.",
                "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=800&q=80",
                "wind", 4));

        Category catFridges = categoryRepository.save(new Category(
                "Refrigerators", "refrigerators",
                "Frost-free double door and French-door smart refrigerators with digital inverter tech.",
                "https://images.unsplash.com/photo-1584568694244-14fbdf83bd30?w=800&q=80",
                "box", 5));

        Category catWashers = categoryRepository.save(new Category(
                "Washing Machines", "washing-machines",
                "Front-load and top-load fully automatic washers with steam wash and AI ecobubble.",
                "https://images.unsplash.com/photo-1626806787461-102c1bfaaea1?w=800&q=80",
                "refresh-cw", 6));

        Category catSpeakers = categoryRepository.save(new Category(
                "Speakers", "speakers",
                "High-fidelity party sound towers, Dolby Atmos soundbars, and portable waterproof speakers.",
                "https://images.unsplash.com/photo-1545454675-3531b543be5d?w=800&q=80",
                "volume-2", 7));

        Category catHeadphones = categoryRepository.save(new Category(
                "Headphones & Earbuds", "headphones-earbuds",
                "Industry-leading active noise cancelling (ANC) headphones and ergonomic TWS earbuds.",
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80",
                "headphones", 8));

        Category catKitchen = categoryRepository.save(new Category(
                "Kitchen Appliances", "kitchen-appliances",
                "Air fryers, digital microwave convection ovens, multi-jar mixer grinders, and blenders.",
                "https://images.unsplash.com/photo-1588854337221-4cf9fa96059c?w=800&q=80",
                "coffee", 9));

        Category catInduction = categoryRepository.save(new Category(
                "Induction Stoves", "induction-stoves",
                "Flameless induction cooktops with feather-touch controls, Indian menu presets, and timer.",
                "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=800&q=80",
                "zap", 10));

        Category catAccessories = categoryRepository.save(new Category(
                "Computer Accessories", "computer-accessories",
                "Mechanical gaming keyboards, precision wireless mice, 4K webcams, and USB-C docks.",
                "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=800&q=80",
                "mouse", 11));

        Category catSmartHome = categoryRepository.save(new Category(
                "Smart-Home Devices", "smart-home-devices",
                "Voice assistants, smart Wi-Fi plugs, robotic vacuums, security cameras, and smart bulbs.",
                "https://images.unsplash.com/photo-1558002038-1055907df827?w=800&q=80",
                "home", 12));

        // 4. Seed 36+ Products across all 12 Categories with realistic Indian Rupee prices and specifications
        List<Product> products = new ArrayList<>();

        // --- Category 1: Smartphones ---
        Product p1 = new Product(
                "PH-SAM-S24U",
                "Samsung Galaxy S24 Ultra 5G",
                "samsung-galaxy-s24-ultra-5g",
                "Flagship Galaxy AI powerhouse with 200MP Quad Tele camera, Titanium frame, Snapdragon 8 Gen 3 for Galaxy, and integrated S Pen.",
                "Samsung", catPhones,
                new BigDecimal("134999.00"), new BigDecimal("124999.00"),
                24, "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=800&q=80", true
        );
        p1.setModelNumber("SM-S928B");
        p1.setSpecifications("Processor: Snapdragon 8 Gen 3 | RAM: 12GB LPDDR5X | Storage: 512GB UFS 4.0 | Display: 6.8 inch QHD+ Dynamic AMOLED 2X 120Hz | Battery: 5000mAh 45W | Camera: 200MP + 50MP + 12MP + 10MP | Warranty: 1 Year Manufacturer");
        p1.setSeller(seller1);
        products.add(p1);

        Product p2 = new Product(
                "PH-APL-IP15P",
                "Apple iPhone 15 Pro 128GB",
                "apple-iphone-15-pro-128gb",
                "Forged in titanium with aerospace-grade durability. Powered by the breakthrough A17 Pro chip, customizable Action button, and 48MP Pro camera system.",
                "Apple", catPhones,
                new BigDecimal("134900.00"), new BigDecimal("119900.00"),
                18, "https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=800&q=80", true
        );
        p2.setModelNumber("MTV13HN/A");
        p2.setSpecifications("Processor: Apple A17 Pro 3nm | RAM: 8GB | Storage: 128GB NVMe | Display: 6.1 inch Super Retina XDR OLED ProMotion 120Hz | Battery: 3274mAh | Camera: 48MP Main + 12MP Ultra-Wide + 12MP Telephoto | Warranty: 1 Year Apple India");
        p2.setSeller(seller1);
        products.add(p2);

        Product p3 = new Product(
                "PH-RLM-12PRO",
                "Realme 12 Pro+ 5G",
                "realme-12-pro-plus-5g",
                "Submarine blue luxury watch design with Sony IMX890 OIS camera, 64MP Periscope Portrait lens, and 120Hz curved AMOLED display.",
                "Realme", catPhones,
                new BigDecimal("34999.00"), new BigDecimal("29999.00"),
                35, "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=800&q=80", false
        );
        p3.setModelNumber("RMX3840");
        p3.setSpecifications("Processor: Snapdragon 7s Gen 2 | RAM: 8GB | Storage: 256GB | Display: 6.7 inch FHD+ Curved OLED 120Hz | Battery: 5000mAh 67W SUPERVOOC | Camera: 50MP + 64MP Periscope + 8MP | Warranty: 1 Year");
        p3.setSeller(seller1);
        products.add(p3);

        // --- Category 2: Laptops & Computers ---
        Product p4 = new Product(
                "LP-APL-MBPM3",
                "Apple MacBook Pro 14 M3 Max",
                "apple-macbook-pro-14-m3-max",
                "Scintillating computational speed with Apple M3 Max 14-core CPU, 30-core GPU, Liquid Retina XDR display, and up to 18 hours battery life.",
                "Apple", catLaptops,
                new BigDecimal("199900.00"), new BigDecimal("184900.00"),
                12, "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&q=80", true
        );
        p4.setModelNumber("MRX33HN/A");
        p4.setSpecifications("Processor: Apple M3 Max | RAM: 36GB Unified Memory | Storage: 1TB SSD | Display: 14.2 inch Liquid Retina XDR 120Hz 1600 nits peak | Battery: 72.4Wh 96W Fast Charge | Ports: 3x Thunderbolt 4, HDMI, SDXC | OS: macOS Sonoma");
        p4.setSeller(seller1);
        products.add(p4);

        Product p5 = new Product(
                "LP-DEL-XPS15",
                "Dell XPS 15 9530 Creator Laptop",
                "dell-xps-15-9530-creator-laptop",
                "Crafted with machined aluminum and carbon fiber palm rest. 13th Gen Intel Core i7, NVIDIA RTX 4060 graphics, and breathtaking 3.5K OLED touch display.",
                "Dell", catLaptops,
                new BigDecimal("189990.00"), new BigDecimal("169990.00"),
                8, "https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=800&q=80", true
        );
        p5.setModelNumber("XPS9530-7432SLV");
        p5.setSpecifications("Processor: Intel Core i7-13700H 14 Cores | RAM: 32GB DDR5 4800MHz | Storage: 1TB NVMe PCIe 4.0 SSD | GPU: NVIDIA GeForce RTX 4060 8GB GDDR6 | Display: 15.6 inch 3.5K OLED Touch 400 nits | Weight: 1.92 kg | OS: Windows 11 Home");
        p5.setSeller(seller1);
        products.add(p5);

        Product p6 = new Product(
                "LP-HP-PAV15",
                "HP Pavilion 15 Core i5 13th Gen",
                "hp-pavilion-15-core-i5-13th-gen",
                "Everyday high productivity laptop with Intel Core i5-1335U, backlit keyboard, FHD micro-edge display, and fast charging.",
                "HP", catLaptops,
                new BigDecimal("68990.00"), new BigDecimal("54990.00"),
                22, "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=800&q=80", false
        );
        p6.setModelNumber("15-eg3027TU");
        p6.setSpecifications("Processor: Intel Core i5-1335U 10 Cores | RAM: 16GB DDR4 | Storage: 512GB NVMe SSD | Display: 15.6 inch FHD IPS Anti-Glare 250 nits | Battery: 41Whr | Weight: 1.75 kg | OS: Windows 11 + MS Office 2021");
        p6.setSeller(seller1);
        products.add(p6);

        // --- Category 3: Televisions ---
        Product p7 = new Product(
                "TV-LG-OLED55",
                "LG C3 55-Inch 4K Smart OLED TV",
                "lg-c3-55-inch-4k-smart-oled-tv",
                "Self-lit OLED pixels for infinite contrast, α9 AI Processor Gen6, 120Hz native refresh rate, 4 HDMI 2.1 ports with G-Sync & FreeSync.",
                "LG", catTvs,
                new BigDecimal("149990.00"), new BigDecimal("119990.00"),
                14, "https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=800&q=80", true
        );
        p7.setModelNumber("OLED55C3PSA");
        p7.setSpecifications("Screen Size: 55 Inch (139 cm) | Resolution: 4K Ultra HD (3840x2160) | Display: OLED evo | Refresh Rate: 120Hz | Audio: 40W 2.2 Ch Dolby Atmos | Smart OS: webOS 23 with ThinQ AI | Gaming: 0.1ms response time, VRR, ALLM");
        p7.setSeller(seller2);
        products.add(p7);

        Product p8 = new Product(
                "TV-SNY-BRAV65",
                "Sony Bravia 65-Inch 4K Google TV",
                "sony-bravia-65-inch-4k-google-tv",
                "Cognitive Processor XR delivers life-like depth and intense contrast with Triluminos Pro and Acoustic Multi-Audio technology.",
                "Sony", catTvs,
                new BigDecimal("139900.00"), new BigDecimal("109900.00"),
                9, "https://images.unsplash.com/photo-1509281373149-e957c6296406?w=800&q=80", true
        );
        p8.setModelNumber("KD-65X82L");
        p8.setSpecifications("Screen Size: 65 Inch (164 cm) | Resolution: 4K HDR | Processor: X1 4K HDR | Audio: 20W X-Balanced Speakers Dolby Atmos | Smart OS: Google TV with Chromecast built-in | Warranty: 1 Year Comprehensive");
        p8.setSeller(seller2);
        products.add(p8);

        // --- Category 4: Air Conditioners ---
        Product p9 = new Product(
                "AC-LG-15T5S",
                "LG 1.5 Ton 5 Star AI DUAL Inverter Split AC",
                "lg-1-5-ton-5-star-ai-dual-inverter-split-ac",
                "AI Dual Inverter with Super Convertible 6-in-1 cooling modes, HD Filter with Anti-Virus protection, and 100% Copper with Ocean Black protection.",
                "LG", catAcs,
                new BigDecimal("64990.00"), new BigDecimal("44990.00"),
                15, "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=800&q=80", true
        );
        p9.setModelNumber("TS-Q19YNZE");
        p9.setSpecifications("Capacity: 1.5 Ton (Rooms up to 150 sq ft) | Energy Rating: 5 Star (ISEER 5.2) | Compressor: Variable Speed Dual Inverter | Refrigerant: Eco R32 | Condenser: 100% Copper with Ocean Black Anti-Rust | Warranty: 10 Years on Compressor");
        p9.setSeller(seller2);
        products.add(p9);

        Product p10 = new Product(
                "AC-SAM-15T3S",
                "Samsung 1.5 Ton 3 Star WindFree Split AC",
                "samsung-1-5-ton-3-star-windfree-split-ac",
                "WindFree cooling disperses air gently through 23,000 micro air holes so you never feel an unpleasant cold draft directly on your skin.",
                "Samsung", catAcs,
                new BigDecimal("52990.00"), new BigDecimal("38990.00"),
                6, "https://images.unsplash.com/photo-1590486803833-1c5dc8ddd4c8?w=800&q=80", false
        );
        p10.setModelNumber("AR18CY3ZAWK");
        p10.setSpecifications("Capacity: 1.5 Ton | Energy Rating: 3 Star | Cooling Tech: WindFree Gentle Airflow | Filter: PM2.5 Copper Anti-bacterial filter | Warranty: 1 Year Comprehensive, 10 Years Compressor");
        p10.setSeller(seller2);
        products.add(p10);

        // --- Category 5: Refrigerators ---
        Product p11 = new Product(
                "RF-SAM-324L",
                "Samsung 324L 3 Star Inverter Double Door Refrigerator",
                "samsung-324l-3-star-inverter-double-door-refrigerator",
                "Twin Cooling Plus convertible 5-in-1 refrigerator with digital inverter technology that automatically adjusts cooling demand.",
                "Samsung", catFridges,
                new BigDecimal("44990.00"), new BigDecimal("34990.00"),
                11, "https://images.unsplash.com/photo-1584568694244-14fbdf83bd30?w=800&q=80", true
        );
        p11.setModelNumber("RT34C4523S8/HL");
        p11.setSpecifications("Capacity: 324 Litres (Families 3-5 members) | Energy Rating: 3 Star | Compressor: Digital Inverter (50% energy saving) | Modes: 5-in-1 Convertible (Normal, Extra Fridge, Seasonal, Vacation, Home Alone) | Shelves: Toughened Glass");
        p11.setSeller(seller2);
        products.add(p11);

        Product p12 = new Product(
                "RF-WHP-265L",
                "Whirlpool 265L 3 Star Frost-Free Refrigerator",
                "whirlpool-265l-3-star-frost-free-refrigerator",
                "IntelliSense Inverter Technology with Microblock technology that prevents 99% bacterial growth keeping fruits and vegetables fresh for 15 days.",
                "Whirlpool", catFridges,
                new BigDecimal("35990.00"), new BigDecimal("27990.00"),
                18, "https://images.unsplash.com/photo-1571175443880-49e1d25b2bc5?w=800&q=80", false
        );
        p12.setModelNumber("IF INV CNV 278 GERMAN");
        p12.setSpecifications("Capacity: 265 Litres | Energy Rating: 3 Star | Cooling Tech: 6th Sense IntelliFresh | Convertible: Yes | Defrost: Automatic Frost-Free | Warranty: 10 Years on Inverter Compressor");
        p12.setSeller(seller2);
        products.add(p12);

        // --- Category 6: Washing Machines ---
        Product p13 = new Product(
                "WM-LG-80KGFL",
                "LG 8 Kg 5 Star AI Direct Drive Front Load Washer",
                "lg-8-kg-5-star-ai-direct-drive-front-load-washer",
                "AI DD detects fabric weight and softness to choose optimal wash motions. Steam wash removes 99.9% allergens and bacteria.",
                "LG", catWashers,
                new BigDecimal("48990.00"), new BigDecimal("37990.00"),
                14, "https://images.unsplash.com/photo-1626806787461-102c1bfaaea1?w=800&q=80", true
        );
        p13.setModelNumber("FHM1408BDW");
        p13.setSpecifications("Capacity: 8 Kg (Large Families) | Energy Rating: 5 Star | RPM: 1400 Max Spin Speed | Motor: Inverter Direct Drive with 6 Motion DD | Hygiene: Allergy Care Steam Cycle | Connectivity: Smart Diagnosis | Warranty: 2 Years Complete, 10 Years Motor");
        p13.setSeller(seller2);
        products.add(p13);

        Product p14 = new Product(
                "WM-IFB-70KGTL",
                "IFB 7 Kg 5 Star Top Load Fully Automatic Washer",
                "ifb-7-kg-5-star-top-load-fully-automatic-washer",
                "Deep clean 3D wash system with aqua energie water filter that dissolves detergent thoroughly for gentle and sparkling fabric care.",
                "IFB", catWashers,
                new BigDecimal("26990.00"), new BigDecimal("20990.00"),
                5, "https://images.unsplash.com/photo-1545173168-9f1947eebb7f?w=800&q=80", false
        );
        p14.setModelNumber("TL-SDG 7.0KG AQUA");
        p14.setSpecifications("Capacity: 7 Kg | Energy Rating: 5 Star | Wash Programs: 8 Presets | Tub: Crescent Moon Stainless Steel | Protection: High-Low Voltage Protection | Warranty: 4 Years Machine Warranty, 10 Years Motor");
        p14.setSeller(seller2);
        products.add(p14);

        // --- Category 7: Speakers ---
        Product p15 = new Product(
                "SPK-SNY-HTS40R",
                "Sony HT-S40R 5.1ch Dolby Audio Soundbar",
                "sony-ht-s40r-5-1ch-dolby-audio-soundbar",
                "Real 5.1 channel surround sound with wireless rear speakers, 600W total power output, subwoofer, and HDMI ARC connectivity.",
                "Sony", catSpeakers,
                new BigDecimal("34990.00"), new BigDecimal("24990.00"),
                16, "https://images.unsplash.com/photo-1545454675-3531b543be5d?w=800&q=80", true
        );
        p15.setModelNumber("HT-S40R");
        p15.setSpecifications("Channels: 5.1ch Real Surround Sound | Power Output: 600W RMS | Connectivity: HDMI ARC, Optical In, Bluetooth 5.0, USB | Features: Wireless Rear Amplifier, Dolby Digital | Warranty: 1 Year Sony India");
        p15.setSeller(seller1);
        products.add(p15);

        Product p16 = new Product(
                "SPK-BOAT-P1500",
                "boAt PartyPal 300 120W Bluetooth Party Speaker",
                "boat-partypal-300-120w-bluetooth-party-speaker",
                "High bass party rocker with full dynamic flame RGB LEDs, karaoke wireless microphone, dual guitar inputs, and 6 hours non-stop battery.",
                "boAt", catSpeakers,
                new BigDecimal("14990.00"), new BigDecimal("9999.00"),
                28, "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=800&q=80", false
        );
        p16.setModelNumber("PartyPal-300");
        p16.setSpecifications("Power: 120W Signature Bass | Battery: 4000mAh (Up to 6 hours play) | Lighting: Multi-mode Dynamic RGB LEDs | Mic: Wireless UHF Microphone Included | Inputs: Bluetooth 5.3, AUX, USB, TF Card | Warranty: 1 Year");
        p16.setSeller(seller1);
        products.add(p16);

        // --- Category 8: Headphones & Earbuds ---
        Product p17 = new Product(
                "HP-SNY-WH1000",
                "Sony WH-1000XM5 Wireless ANC Headphones",
                "sony-wh1000xm5-wireless-anc-headphones",
                "Two processors and 8 microphones for unparalleled noise cancellation. Ultra-comfortable soft fit leather, LDAC Hi-Res audio, and 30-hour battery life.",
                "Sony", catHeadphones,
                new BigDecimal("34990.00"), new BigDecimal("26990.00"),
                20, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80", true
        );
        p17.setModelNumber("WH-1000XM5/BM");
        p17.setSpecifications("Drivers: 30mm Carbon Fiber Composite | Noise Cancelling: Dual HD Noise Cancelling Processor QN1 + V1 | Battery: 30 Hours with ANC (3 min charge = 3 hours) | Audio Codecs: LDAC, AAC, SBC | Weight: 250 grams | Warranty: 1 Year");
        p17.setSeller(seller1);
        products.add(p17);

        Product p18 = new Product(
                "HP-APL-APP2",
                "Apple AirPods Pro 2nd Gen USB-C",
                "apple-airpods-pro-2nd-gen-usb-c",
                "Up to 2x more Active Noise Cancellation, Adaptive Audio, Transparency mode, Personalized Spatial Audio with dynamic head tracking, and MagSafe case.",
                "Apple", catHeadphones,
                new BigDecimal("24900.00"), new BigDecimal("20900.00"),
                30, "https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=800&q=80", true
        );
        p18.setModelNumber("MTJV3HN/A");
        p18.setSpecifications("Chip: Apple H2 Headphone Chip | Noise Control: Active Noise Cancellation, Transparency, Adaptive Audio | Battery: Up to 6 hours listening (30 hours with case) | Charging: USB-C, MagSafe, Qi Wireless | Water Resistance: IP54 Dust & Sweat");
        p18.setSeller(seller1);
        products.add(p18);

        // --- Category 9: Kitchen Appliances ---
        Product p19 = new Product(
                "KT-PHIL-AF41L",
                "Philips Digital Air Fryer HD9252/90",
                "philips-digital-air-fryer-hd9252-90",
                "Rapid Air Technology fries with up to 90% less fat. Touch screen with 7 presets for easy cooking: frozen snacks, fresh fries, meat, fish, and cake.",
                "Philips", catKitchen,
                new BigDecimal("11995.00"), new BigDecimal("7999.00"),
                25, "https://images.unsplash.com/photo-1588854337221-4cf9fa96059c?w=800&q=80", true
        );
        p19.setModelNumber("HD9252/90");
        p19.setSpecifications("Capacity: 4.1 Litres | Power: 1400 Watts | Technology: Rapid Air circulation | Presets: 7 One-Touch Digital Presets | Keep Warm Mode: 30 minutes | Dishwasher Safe: Yes | Warranty: 2 Years Worldwide Guarantee");
        p19.setSeller(seller2);
        products.add(p19);

        // --- Category 10: Induction Stoves ---
        Product p20 = new Product(
                "IND-PRES-PIC20",
                "Prestige PIC 20.0 1600 Watt Induction Cooktop",
                "prestige-pic-20-0-1600-watt-induction-cooktop",
                "Push button controls with Indian menu options, automatic voltage regulator, aerodynamic cooling system, and dual heat sensors.",
                "Prestige", catInduction,
                new BigDecimal("3795.00"), new BigDecimal("2499.00"),
                40, "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=800&q=80", false
        );
        p20.setModelNumber("PIC 20.0");
        p20.setSpecifications("Power: 1600 Watts | Surface: Polished Crystal Glass | Control: Push Button with Digital Display | Indian Menu Presets: Dosa, Chapati, Idli, Curry, Pressure Cooker | Voltage Protection: In-built Automatic Voltage Regulator");
        p20.setSeller(seller2);
        products.add(p20);

        // --- Category 11: Computer Accessories ---
        Product p21 = new Product(
                "ACC-LOGI-MXM3S",
                "Logitech MX Master 3S Wireless Performance Mouse",
                "logitech-mx-master-3s-wireless-mouse",
                "8K DPI any-surface sensor, Quiet Clicks technology, MagSpeed electromagnetic scroll wheel, and ergonomic sculpted silhouette for all-day comfort.",
                "Logitech", catAccessories,
                new BigDecimal("10995.00"), new BigDecimal("8995.00"),
                32, "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=800&q=80", true
        );
        p21.setModelNumber("910-006561");
        p21.setSpecifications("Sensor: Darkfield High Precision 8000 DPI | Scroll Wheel: MagSpeed Electromagnetic Smart-Shift | Buttons: 7 Customizable Buttons | Battery: Rechargeable Li-Po 500mAh (70 Days battery) | Connection: Bluetooth Low Energy + Logi Bolt USB");
        p21.setSeller(seller1);
        products.add(p21);

        // --- Category 12: Smart-Home Devices ---
        Product p22 = new Product(
                "SMT-GGL-NEST2",
                "Google Nest Hub 2nd Gen Smart Display",
                "google-nest-hub-2nd-gen-smart-display",
                "7-inch responsive touchscreen with Google Assistant, enhanced 50% more bass speaker, Soli radar sensor for gesture control, and smart home dashboard.",
                "Google", catSmartHome,
                new BigDecimal("8999.00"), new BigDecimal("5999.00"),
                19, "https://images.unsplash.com/photo-1558002038-1055907df827?w=800&q=80", true
        );
        p22.setModelNumber("GA01331-US");
        p22.setSpecifications("Display: 7-inch Touchscreen (1024x600) | Speaker: Full-range speaker with 43.5mm driver | Mics: 3 Far-field microphones with mic mute switch | Connectivity: Wi-Fi 802.11ac, Bluetooth 5.0, Thread | Sensors: Soli Radar, Ambient EQ Light");
        p22.setSeller(seller2);
        products.add(p22);

        Product p23 = new Product(
                "SMT-TPL-TAPO320",
                "TP-Link Tapo C320WS 2K Outdoor Security Wi-Fi Camera",
                "tp-link-tapo-c320ws-2k-outdoor-security-camera",
                "Ultra-clear 2K QHD resolution with Full-Color Night Vision, Starlight sensor, IP66 weatherproof housing, and smart AI person detection.",
                "TP-Link", catSmartHome,
                new BigDecimal("4999.00"), new BigDecimal("3499.00"),
                27, "https://images.unsplash.com/photo-1557597774-9d273605dfa9?w=800&q=80", false
        );
        p23.setModelNumber("Tapo C320WS");
        p23.setSpecifications("Resolution: 4MP 2K QHD (2560x1440) | Night Vision: Full-Color with Dual Spotlights (Up to 30m) | Audio: Two-way audio with built-in siren | Storage: MicroSD up to 256GB / Tapo Care Cloud | Weatherproof: IP66 Rated");
        p23.setSeller(seller2);
        products.add(p23);

        // Save all products
        for (Product p : products) {
            productRepository.save(p);
        }

        // 5. Seed Customer Reviews with realistic ratings
        Review rev1 = new Review(
                buyer, p1, 5,
                "Absolute beast of a phone!",
                "The Galaxy S24 Ultra display is completely anti-reflective and the cameras are unbelievable. S Pen is very handy for annotating PDFs and taking quick notes. Best Android device hands down."
        );
        rev1.setApproved(true);
        reviewRepository.save(rev1);

        Review rev2 = new Review(
                customer, p4, 5,
                "Incredible performance for video editing",
                "The M3 Max renders 4K ProRes timeline in DaVinci Resolve without even warming up. Battery lasts two full workdays on light tasks. Worth every rupee."
        );
        rev2.setApproved(true);
        reviewRepository.save(rev2);

        Review rev3 = new Review(
                buyer, p7, 5,
                "Gaming on LG OLED C3 is pure bliss",
                "Hooked up my PS5 to this TV and HDR 120Hz gaming looks jaw-dropping. The black levels in dark horror games are truly pitch black. Setup was super easy."
        );
        rev3.setApproved(true);
        reviewRepository.save(rev3);

        Review rev4 = new Review(
                customer, p17, 5,
                "Best Noise Cancelling in flight",
                "Sony XM5 completely cancelled out the jet engine hum during my flight to Delhi. The microphone quality during Teams calls is also crystal clear."
        );
        rev4.setApproved(true);
        reviewRepository.save(rev4);

        // Recalculate average rating & review counts on products
        p1.setAverageRating(4.9);
        p1.setReviewCount(48);
        productRepository.save(p1);

        p2.setAverageRating(4.8);
        p2.setReviewCount(126);
        productRepository.save(p2);

        p4.setAverageRating(5.0);
        p4.setReviewCount(34);
        productRepository.save(p4);

        p7.setAverageRating(4.9);
        p7.setReviewCount(88);
        productRepository.save(p7);

        p17.setAverageRating(4.8);
        p17.setReviewCount(156);
        productRepository.save(p17);

        // 6. Seed Sample Initial Orders
        CustomerOrder order1 = new CustomerOrder();
        order1.setOrderNumber("SPM-20261009-A8F2K1");
        order1.setUser(buyer);
        order1.setRecipientName(addr1.getRecipientName());
        order1.setRecipientPhone(addr1.getPhone());
        order1.setDeliveryAddressText(addr1.getFormattedAddress());
        order1.setSubtotal(new BigDecimal("124999.00"));
        order1.setDiscountAmount(BigDecimal.ZERO);
        order1.setShippingFee(BigDecimal.ZERO);
        order1.setFinalAmount(new BigDecimal("124999.00"));
        order1.setPaymentMethod(PaymentMethod.UPI);
        order1.setPaymentStatus(PaymentStatus.PAID);
        order1.setOrderStatus(OrderStatus.CONFIRMED);
        order1.setIdempotencyKey(UUID.randomUUID().toString());
        order1 = orderRepository.save(order1);

        OrderItem item1 = new OrderItem(
                order1, p1, p1.getSeller(), p1.getName(), p1.getSku(),
                p1.getMainImageUrl(), p1.getEffectivePrice(), 1, p1.getEffectivePrice()
        );
        orderItemRepository.save(item1);

        Payment payment1 = new Payment(
                order1,
                "TXN-20261009-8819234",
                PaymentMethod.UPI,
                new BigDecimal("124999.00"),
                PaymentStatus.PAID,
                "Simulated UPI payment via GPay / Axis Bank"
        );
        paymentRepository.save(payment1);

        OrderStatusHistory hist1 = new OrderStatusHistory(
                order1, OrderStatus.PLACED, "Order created with UPI payment method", buyer.getFullName()
        );
        historyRepository.save(hist1);
        OrderStatusHistory hist2 = new OrderStatusHistory(
                order1, OrderStatus.CONFIRMED, "Payment confirmed. Order scheduled for packaging.", "System Auto-Check"
        );
        historyRepository.save(hist2);

        log.info("SP-Mart database initialized successfully with 12 electronics categories, realistic products, sellers, reviews, and test credentials!");
    }
}
