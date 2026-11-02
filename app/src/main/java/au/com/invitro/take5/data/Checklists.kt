package au.com.invitro.take5.data

object Checklists {

    val machineTypes = listOf(
        "Washer/Disinfector",
        "Steam Steriliser",
        "Low Temperature Steriliser",
        "Heat Sealer",
        "Wash Racks / Load Carriers",
        "Trolleys",
        "Conveyor / Rack Return System",
        "Ultrasonic Cleaner",
        "Drying Cabinet",
        "Endoscope Reprocessor",
        "Water Treatment / RO Plant",
        "Other",
    )

    val jobTypes = listOf(
        "Installation",
        "Preventive Maintenance / Service",
        "Breakdown / Repair",
        "Validation / Testing",
        "Commissioning",
        "Decommission / Removal",
        "Inspection / Quote",
        "Other",
    )

    val hazards = listOf(
        "Hot surfaces, steam or hot water",
        "Pressurised systems (steam, air, water)",
        "Electrical (live work, 3-phase, stored energy)",
        "Chemicals (detergents, H2O2, peracetic acid, descalers)",
        "Biological / contaminated instruments or water",
        "Sharps",
        "Manual handling / heavy lifting",
        "Moving parts, doors, conveyors, pinch points",
        "Wet floors, slips, trips & falls",
        "Working at height (ladders, plant rooms)",
        "Confined or restricted space",
        "Noise",
        "Hot work (welding, brazing, soldering)",
        "Building services (gas, water, drainage)",
        "Dust, silica or asbestos (wall/floor penetrations)",
        "Hospital staff, patients or public in work area",
        "Working alone, after hours or fatigue",
    )

    val controls = listOf(
        "Equipment isolated and locked out / tagged out (LOTO)",
        "Steam / pressure released and zero energy verified",
        "Equipment allowed to cool",
        "Correct PPE worn (gloves, glasses, safety boots, hearing)",
        "SDS reviewed for chemicals in use",
        "Area barricaded and signage in place",
        "CSSD / department manager notified",
        "Site permit obtained (hot work, isolation, height)",
        "Infection control requirements followed",
        "Mechanical aids or team lift for heavy items",
        "Spill kit and first aid accessible",
        "Tools and test equipment inspected (test & tag current)",
    )

    val readinessQuestions = listOf(
        "Do I have the skills, training and licences for this task?",
        "Do I have the right tools, test equipment and PPE?",
        "Has the site / department contact been notified?",
        "Can the equipment be safely isolated (electrical, steam, water, air, chemical)?",
        "Have I checked site permits and requirements (hot work, infection control)?",
        "Do I know the emergency procedure and assembly point?",
    )

    val answers = listOf("Yes", "No", "N/A")

    const val DECLARATION =
        "I have stopped and thought about this job, identified the hazards and put controls in place. " +
            "I will stop work and reassess if conditions change."
}
