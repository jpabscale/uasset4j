// GENERATED FILE — DO NOT EDIT BY HAND.
// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/ObjectVersion.cs
// Regenerate with: python3 tools/gen_objectversion.py (see header of this file).
package com.github.jpabscale.uasset4j.unrealtypes

enum class ObjectVersion(val value: Int) {
    /** <summary>Unknown version</summary> */
    UNKNOWN(0),
    /** <summary>VER_UE4_OLDEST_LOADABLE_PACKAGE</summary> */
    VER_UE4_OLDEST_LOADABLE_PACKAGE(214),
    /** <summary>Removed restriction on blueprint-exposed variables from being read-only</summary> */
    VER_UE4_BLUEPRINT_VARS_NOT_READ_ONLY(215),
    /** <summary>Added manually serialized element to UStaticMesh (precalculated nav collision)</summary> */
    VER_UE4_STATIC_MESH_STORE_NAV_COLLISION(216),
    /** <summary>Changed property name for atmospheric fog</summary> */
    VER_UE4_ATMOSPHERIC_FOG_DECAY_NAME_CHANGE(217),
    /** <summary>Change many properties/functions from Translation to Location</summary> */
    VER_UE4_SCENECOMP_TRANSLATION_TO_LOCATION(218),
    /** <summary>Material attributes reordering</summary> */
    VER_UE4_MATERIAL_ATTRIBUTES_REORDERING(219),
    /** <summary>Collision Profile setting has been added, and all components that exists has to be properly upgraded</summary> */
    VER_UE4_COLLISION_PROFILE_SETTING(220),
    /** <summary>Making the blueprint's skeleton class transient</summary> */
    VER_UE4_BLUEPRINT_SKEL_TEMPORARY_TRANSIENT(221),
    /** <summary>Making the blueprint's skeleton class serialized again</summary> */
    VER_UE4_BLUEPRINT_SKEL_SERIALIZED_AGAIN(222),
    /** <summary>Blueprint now controls replication settings again</summary> */
    VER_UE4_BLUEPRINT_SETS_REPLICATION(223),
    /** <summary>Added level info used by World browser</summary> */
    VER_UE4_WORLD_LEVEL_INFO(224),
    /** <summary>Changed capsule height to capsule half-height (afterwards)</summary> */
    VER_UE4_AFTER_CAPSULE_HALF_HEIGHT_CHANGE(225),
    /** <summary>Added Namepace, GUID (Key) and Flags to FText</summary> */
    VER_UE4_ADDED_NAMESPACE_AND_KEY_DATA_TO_FTEXT(226),
    /** <summary>Attenuation shapes</summary> */
    VER_UE4_ATTENUATION_SHAPES(227),
    /** <summary>Use IES texture multiplier even when IES brightness is not being used</summary> */
    VER_UE4_LIGHTCOMPONENT_USE_IES_TEXTURE_MULTIPLIER_ON_NON_IES_BRIGHTNESS(228),
    /** <summary>Removed InputComponent as a blueprint addable component</summary> */
    VER_UE4_REMOVE_INPUT_COMPONENTS_FROM_BLUEPRINTS(229),
    /** <summary>Use an FMemberReference struct in UK2Node_Variable</summary> */
    VER_UE4_VARK2NODE_USE_MEMBERREFSTRUCT(230),
    /** <summary>Refactored material expression inputs for UMaterialExpressionSceneColor and UMaterialExpressionSceneDepth</summary> */
    VER_UE4_REFACTOR_MATERIAL_EXPRESSION_SCENECOLOR_AND_SCENEDEPTH_INPUTS(231),
    /** <summary>Spline meshes changed from Z forwards to configurable</summary> */
    VER_UE4_SPLINE_MESH_ORIENTATION(232),
    /** <summary>Added ReverbEffect asset type</summary> */
    VER_UE4_REVERB_EFFECT_ASSET_TYPE(233),
    /** <summary>changed max texcoords from 4 to 8</summary> */
    VER_UE4_MAX_TEXCOORD_INCREASED(234),
    /** <summary>static meshes changed to support SpeedTrees</summary> */
    VER_UE4_SPEEDTREE_STATICMESH(235),
    /** <summary>Landscape component reference between landscape component and collision component</summary> */
    VER_UE4_LANDSCAPE_COMPONENT_LAZY_REFERENCES(236),
    /** <summary>Refactored UK2Node_CallFunction to use FMemberReference</summary> */
    VER_UE4_SWITCH_CALL_NODE_TO_USE_MEMBER_REFERENCE(237),
    /** <summary>Added fixup step to remove skeleton class references from blueprint objects</summary> */
    VER_UE4_ADDED_SKELETON_ARCHIVER_REMOVAL(238),
    /** <summary>See above, take 2.</summary> */
    VER_UE4_ADDED_SKELETON_ARCHIVER_REMOVAL_SECOND_TIME(239),
    /** <summary>Making the skeleton class on blueprints transient</summary> */
    VER_UE4_BLUEPRINT_SKEL_CLASS_TRANSIENT_AGAIN(240),
    /** <summary>UClass knows if it's been cooked</summary> */
    VER_UE4_ADD_COOKED_TO_UCLASS(241),
    /** <summary>Deprecated static mesh thumbnail properties were removed</summary> */
    VER_UE4_DEPRECATED_STATIC_MESH_THUMBNAIL_PROPERTIES_REMOVED(242),
    /** <summary>Added collections in material shader map ids</summary> */
    VER_UE4_COLLECTIONS_IN_SHADERMAPID(243),
    /** <summary>Renamed some Movement Component properties, added PawnMovementComponent</summary> */
    VER_UE4_REFACTOR_MOVEMENT_COMPONENT_HIERARCHY(244),
    /** <summary>Swap UMaterialExpressionTerrainLayerSwitch::LayerUsed/LayerNotUsed the correct way round</summary> */
    VER_UE4_FIX_TERRAIN_LAYER_SWITCH_ORDER(245),
    /** <summary>Remove URB_ConstraintSetup</summary> */
    VER_UE4_ALL_PROPS_TO_CONSTRAINTINSTANCE(246),
    /** <summary>Low quality directional lightmaps</summary> */
    VER_UE4_LOW_QUALITY_DIRECTIONAL_LIGHTMAPS(247),
    /** <summary>Added NoiseEmitterComponent and removed related Pawn properties.</summary> */
    VER_UE4_ADDED_NOISE_EMITTER_COMPONENT(248),
    /** <summary>Add text component vertical alignment</summary> */
    VER_UE4_ADD_TEXT_COMPONENT_VERTICAL_ALIGNMENT(249),
    /** <summary>Added AssetImportData for FBX asset types, deprecating SourceFilePath and SourceFileTimestamp</summary> */
    VER_UE4_ADDED_FBX_ASSET_IMPORT_DATA(250),
    /** <summary>Remove LevelBodySetup from ULevel</summary> */
    VER_UE4_REMOVE_LEVELBODYSETUP(251),
    /** <summary>Refactor character crouching</summary> */
    VER_UE4_REFACTOR_CHARACTER_CROUCH(252),
    /** <summary>Trimmed down material shader debug information.</summary> */
    VER_UE4_SMALLER_DEBUG_MATERIALSHADER_UNIFORM_EXPRESSIONS(253),
    /** <summary>APEX Clothing</summary> */
    VER_UE4_APEX_CLOTH(254),
    /** <summary> Change Collision Channel to save only modified ones than all of them. Note!!! Once we pass this CL, we can rename FCollisionResponseContainer enum values. We should rename to match ECollisionChannel </summary> */
    VER_UE4_SAVE_COLLISIONRESPONSE_PER_CHANNEL(255),
    /** <summary>Added Landscape Spline editor meshes</summary> */
    VER_UE4_ADDED_LANDSCAPE_SPLINE_EDITOR_MESH(256),
    /** <summary>Fixup input expressions for reading from refraction material attributes.</summary> */
    VER_UE4_CHANGED_MATERIAL_REFACTION_TYPE(257),
    /** <summary>Refactor projectile movement, along with some other movement component work.</summary> */
    VER_UE4_REFACTOR_PROJECTILE_MOVEMENT(258),
    /** <summary>Remove PhysicalMaterialProperty and replace with user defined enum</summary> */
    VER_UE4_REMOVE_PHYSICALMATERIALPROPERTY(259),
    /** <summary>Removed all compile outputs from FMaterial</summary> */
    VER_UE4_PURGED_FMATERIAL_COMPILE_OUTPUTS(260),
    /** <summary>Ability to save cooked PhysX meshes to Landscape</summary> */
    VER_UE4_ADD_COOKED_TO_LANDSCAPE(261),
    /** <summary>Change how input component consumption works</summary> */
    VER_UE4_CONSUME_INPUT_PER_BIND(262),
    /** <summary>Added new Graph based SoundClass Editor</summary> */
    VER_UE4_SOUND_CLASS_GRAPH_EDITOR(263),
    /** <summary>Fixed terrain layer node guids which was causing artifacts</summary> */
    VER_UE4_FIXUP_TERRAIN_LAYER_NODES(264),
    /** <summary>Added clamp min/max swap check to catch older materials</summary> */
    VER_UE4_RETROFIT_CLAMP_EXPRESSIONS_SWAP(265),
    /** <summary>Remove static/movable/stationary light classes</summary> */
    VER_UE4_REMOVE_LIGHT_MOBILITY_CLASSES(266),
    /** <summary>Refactor the way physics blending works to allow partial blending</summary> */
    VER_UE4_REFACTOR_PHYSICS_BLENDING(267),
    /** <summary>WorldLevelInfo: Added reference to parent level and streaming distance</summary> */
    VER_UE4_WORLD_LEVEL_INFO_UPDATED(268),
    /** <summary>Fixed cooking of skeletal/static meshes due to bad serialization logic</summary> */
    VER_UE4_STATIC_SKELETAL_MESH_SERIALIZATION_FIX(269),
    /** <summary>Removal of InterpActor and PhysicsActor</summary> */
    VER_UE4_REMOVE_STATICMESH_MOBILITY_CLASSES(270),
    /** <summary>Refactor physics transforms</summary> */
    VER_UE4_REFACTOR_PHYSICS_TRANSFORMS(271),
    /** <summary>Remove zero triangle sections from static meshes and compact material indices.</summary> */
    VER_UE4_REMOVE_ZERO_TRIANGLE_SECTIONS(272),
    /** <summary>Add param for deceleration in character movement instead of using acceleration.</summary> */
    VER_UE4_CHARACTER_MOVEMENT_DECELERATION(273),
    /** <summary>Made ACameraActor use a UCameraComponent for parameter storage, etc...</summary> */
    VER_UE4_CAMERA_ACTOR_USING_CAMERA_COMPONENT(274),
    /** <summary>Deprecated some pitch/roll properties in CharacterMovementComponent</summary> */
    VER_UE4_CHARACTER_MOVEMENT_DEPRECATE_PITCH_ROLL(275),
    /** <summary>Rebuild texture streaming data on load for uncooked builds</summary> */
    VER_UE4_REBUILD_TEXTURE_STREAMING_DATA_ON_LOAD(276),
    /** <summary>Add support for 32 bit index buffers for static meshes.</summary> */
    VER_UE4_SUPPORT_32BIT_STATIC_MESH_INDICES(277),
    /** <summary>Added streaming install ChunkID to AssetData and UPackage</summary> */
    VER_UE4_ADDED_CHUNKID_TO_ASSETDATA_AND_UPACKAGE(278),
    /** <summary>Add flag to control whether Character blueprints receive default movement bindings.</summary> */
    VER_UE4_CHARACTER_DEFAULT_MOVEMENT_BINDINGS(279),
    /** <summary>APEX Clothing LOD Info</summary> */
    VER_UE4_APEX_CLOTH_LOD(280),
    /** <summary>Added atmospheric fog texture data to be general</summary> */
    VER_UE4_ATMOSPHERIC_FOG_CACHE_DATA(281),
    /** <summary>Arrays serialize their inner's tags</summary> */
    VAR_UE4_ARRAY_PROPERTY_INNER_TAGS(282),
    /** <summary>Skeletal mesh index data is kept in memory in game to support mesh merging.</summary> */
    VER_UE4_KEEP_SKEL_MESH_INDEX_DATA(283),
    /** <summary>Added compatibility for the body instance collision change</summary> */
    VER_UE4_BODYSETUP_COLLISION_CONVERSION(284),
    /** <summary>Reflection capture cooking</summary> */
    VER_UE4_REFLECTION_CAPTURE_COOKING(285),
    /** <summary>Removal of DynamicTriggerVolume, DynamicBlockingVolume, DynamicPhysicsVolume</summary> */
    VER_UE4_REMOVE_DYNAMIC_VOLUME_CLASSES(286),
    /** <summary>Store an additional flag in the BodySetup to indicate whether there is any cooked data to load</summary> */
    VER_UE4_STORE_HASCOOKEDDATA_FOR_BODYSETUP(287),
    /** <summary>Changed name of RefractionBias to RefractionDepthBias.</summary> */
    VER_UE4_REFRACTION_BIAS_TO_REFRACTION_DEPTH_BIAS(288),
    /** <summary>Removal of SkeletalPhysicsActor</summary> */
    VER_UE4_REMOVE_SKELETALPHYSICSACTOR(289),
    /** <summary>PlayerController rotation input refactor</summary> */
    VER_UE4_PC_ROTATION_INPUT_REFACTOR(290),
    /** <summary>Landscape Platform Data cooking</summary> */
    VER_UE4_LANDSCAPE_PLATFORMDATA_COOKING(291),
    /** <summary>Added call for linking classes in CreateExport to ensure memory is initialized properly</summary> */
    VER_UE4_CREATEEXPORTS_CLASS_LINKING_FOR_BLUEPRINTS(292),
    /** <summary>Remove native component nodes from the blueprint SimpleConstructionScript</summary> */
    VER_UE4_REMOVE_NATIVE_COMPONENTS_FROM_BLUEPRINT_SCS(293),
    /** <summary>Removal of Single Node Instance</summary> */
    VER_UE4_REMOVE_SINGLENODEINSTANCE(294),
    /** <summary>Character movement braking changes</summary> */
    VER_UE4_CHARACTER_BRAKING_REFACTOR(295),
    /** <summary>Supported low quality lightmaps in volume samples</summary> */
    VER_UE4_VOLUME_SAMPLE_LOW_QUALITY_SUPPORT(296),
    /** <summary>Split bEnableTouchEvents out from bEnableClickEvents</summary> */
    VER_UE4_SPLIT_TOUCH_AND_CLICK_ENABLES(297),
    /** <summary>Health/Death refactor</summary> */
    VER_UE4_HEALTH_DEATH_REFACTOR(298),
    /** <summary>Moving USoundNodeEnveloper from UDistributionFloatConstantCurve to FRichCurve</summary> */
    VER_UE4_SOUND_NODE_ENVELOPER_CURVE_CHANGE(299),
    /** <summary>Moved SourceRadius to UPointLightComponent</summary> */
    VER_UE4_POINT_LIGHT_SOURCE_RADIUS(300),
    /** <summary>Scene capture actors based on camera actors.</summary> */
    VER_UE4_SCENE_CAPTURE_CAMERA_CHANGE(301),
    /** <summary>Moving SkeletalMesh shadow casting flag from LoD details to material</summary> */
    VER_UE4_MOVE_SKELETALMESH_SHADOWCASTING(302),
    /** <summary>Changing bytecode operators for creating arrays</summary> */
    VER_UE4_CHANGE_SETARRAY_BYTECODE(303),
    /** <summary>Material Instances overriding base material properties.</summary> */
    VER_UE4_MATERIAL_INSTANCE_BASE_PROPERTY_OVERRIDES(304),
    /** <summary>Combined top/bottom lightmap textures</summary> */
    VER_UE4_COMBINED_LIGHTMAP_TEXTURES(305),
    /** <summary>Forced material lightmass guids to be regenerated</summary> */
    VER_UE4_BUMPED_MATERIAL_EXPORT_GUIDS(306),
    /** <summary>Allow overriding of parent class input bindings</summary> */
    VER_UE4_BLUEPRINT_INPUT_BINDING_OVERRIDES(307),
    /** <summary>Fix up convex invalid transform</summary> */
    VER_UE4_FIXUP_BODYSETUP_INVALID_CONVEX_TRANSFORM(308),
    /** <summary>Fix up scale of physics stiffness and damping value</summary> */
    VER_UE4_FIXUP_STIFFNESS_AND_DAMPING_SCALE(309),
    /** <summary>Convert USkeleton and FBoneContrainer to using FReferenceSkeleton.</summary> */
    VER_UE4_REFERENCE_SKELETON_REFACTOR(310),
    /** <summary>Adding references to variable, function, and macro nodes to be able to update to renamed values</summary> */
    VER_UE4_K2NODE_REFERENCEGUIDS(311),
    /** <summary>Fix up the 0th bone's parent bone index.</summary> */
    VER_UE4_FIXUP_ROOTBONE_PARENT(312),
    VER_UE4_TEXT_RENDER_COMPONENTS_WORLD_SPACE_SIZING(313),
    /** <summary>Material Instances overriding base material properties #2.</summary> */
    VER_UE4_MATERIAL_INSTANCE_BASE_PROPERTY_OVERRIDES_PHASE_2(314),
    /** <summary>CLASS_Placeable becomes CLASS_NotPlaceable</summary> */
    VER_UE4_CLASS_NOTPLACEABLE_ADDED(315),
    /** <summary>Added LOD info list to a world tile description</summary> */
    VER_UE4_WORLD_LEVEL_INFO_LOD_LIST(316),
    /** <summary>CharacterMovement variable naming refactor</summary> */
    VER_UE4_CHARACTER_MOVEMENT_VARIABLE_RENAMING_1(317),
    /** <summary>FName properties containing sound names converted to FSlateSound properties</summary> */
    VER_UE4_FSLATESOUND_CONVERSION(318),
    /** <summary>Added ZOrder to a world tile description</summary> */
    VER_UE4_WORLD_LEVEL_INFO_ZORDER(319),
    /** <summary>Added flagging of localization gather requirement to packages</summary> */
    VER_UE4_PACKAGE_REQUIRES_LOCALIZATION_GATHER_FLAGGING(320),
    /** <summary>Preventing Blueprint Actor variables from having default values</summary> */
    VER_UE4_BP_ACTOR_VARIABLE_DEFAULT_PREVENTING(321),
    /** <summary>Preventing Blueprint Actor variables from having default values</summary> */
    VER_UE4_TEST_ANIMCOMP_CHANGE(322),
    /** <summary>Class as primary asset, name convention changed</summary> */
    VER_UE4_EDITORONLY_BLUEPRINTS(323),
    /** <summary>Custom serialization for FEdGraphPinType</summary> */
    VER_UE4_EDGRAPHPINTYPE_SERIALIZATION(324),
    /** <summary>Stop generating 'mirrored' cooked mesh for Brush and Model components</summary> */
    VER_UE4_NO_MIRROR_BRUSH_MODEL_COLLISION(325),
    /** <summary>Changed ChunkID to be an array of IDs.</summary> */
    VER_UE4_CHANGED_CHUNKID_TO_BE_AN_ARRAY_OF_CHUNKIDS(326),
    /** <summary>Worlds have been renamed from "TheWorld" to be named after the package containing them</summary> */
    VER_UE4_WORLD_NAMED_AFTER_PACKAGE(327),
    /** <summary>Added sky light component</summary> */
    VER_UE4_SKY_LIGHT_COMPONENT(328),
    /** <summary>Added Enable distance streaming flag to FWorldTileLayer</summary> */
    VER_UE4_WORLD_LAYER_ENABLE_DISTANCE_STREAMING(329),
    /** <summary>Remove visibility/zone information from UModel</summary> */
    VER_UE4_REMOVE_ZONES_FROM_MODEL(330),
    /** <summary>Fix base pose serialization </summary> */
    VER_UE4_FIX_ANIMATIONBASEPOSE_SERIALIZATION(331),
    /** <summary>Support for up to 8 skinning influences per vertex on skeletal meshes (on non-gpu vertices)</summary> */
    VER_UE4_SUPPORT_8_BONE_INFLUENCES_SKELETAL_MESHES(332),
    /** <summary>Add explicit bOverrideGravity to world settings</summary> */
    VER_UE4_ADD_OVERRIDE_GRAVITY_FLAG(333),
    /** <summary>Support for up to 8 skinning influences per vertex on skeletal meshes (on gpu vertices)</summary> */
    VER_UE4_SUPPORT_GPUSKINNING_8_BONE_INFLUENCES(334),
    /** <summary>Supporting nonuniform scale animation</summary> */
    VER_UE4_ANIM_SUPPORT_NONUNIFORM_SCALE_ANIMATION(335),
    /** <summary>Engine version is stored as a FEngineVersion object rather than changelist number</summary> */
    VER_UE4_ENGINE_VERSION_OBJECT(336),
    /** <summary>World assets now have RF_Public</summary> */
    VER_UE4_PUBLIC_WORLDS(337),
    /** <summary>Skeleton Guid</summary> */
    VER_UE4_SKELETON_GUID_SERIALIZATION(338),
    /** <summary>Character movement WalkableFloor refactor</summary> */
    VER_UE4_CHARACTER_MOVEMENT_WALKABLE_FLOOR_REFACTOR(339),
    /** <summary>Lights default to inverse squared</summary> */
    VER_UE4_INVERSE_SQUARED_LIGHTS_DEFAULT(340),
    /** <summary>Disabled SCRIPT_LIMIT_BYTECODE_TO_64KB</summary> */
    VER_UE4_DISABLED_SCRIPT_LIMIT_BYTECODE(341),
    /** <summary>Made remote role private, exposed bReplicates</summary> */
    VER_UE4_PRIVATE_REMOTE_ROLE(342),
    /** <summary>Fix up old foliage components to have static mobility (superseded by VER_UE4_FOLIAGE_MOVABLE_MOBILITY)</summary> */
    VER_UE4_FOLIAGE_STATIC_MOBILITY(343),
    /** <summary>Change BuildScale from a float to a vector</summary> */
    VER_UE4_BUILD_SCALE_VECTOR(344),
    /** <summary>After implementing foliage collision, need to disable collision on old foliage instances</summary> */
    VER_UE4_FOLIAGE_COLLISION(345),
    /** <summary>Added sky bent normal to indirect lighting cache</summary> */
    VER_UE4_SKY_BENT_NORMAL(346),
    /** <summary>Added cooking for landscape collision data</summary> */
    VER_UE4_LANDSCAPE_COLLISION_DATA_COOKING(347),
    /** <summary> Convert CPU tangent Z delta to vector from PackedNormal since we don't get any benefit other than memory we still convert all to FVector in CPU time whenever any calculation </summary> */
    VER_UE4_MORPHTARGET_CPU_TANGENTZDELTA_FORMATCHANGE(348),
    /** <summary>Soft constraint limits will implicitly use the mass of the bodies</summary> */
    VER_UE4_SOFT_CONSTRAINTS_USE_MASS(349),
    /** <summary>Reflection capture data saved in packages</summary> */
    VER_UE4_REFLECTION_DATA_IN_PACKAGES(350),
    /** <summary>Fix up old foliage components to have movable mobility (superseded by VER_UE4_FOLIAGE_STATIC_LIGHTING_SUPPORT)</summary> */
    VER_UE4_FOLIAGE_MOVABLE_MOBILITY(351),
    /** <summary>Undo BreakMaterialAttributes changes as it broke old content</summary> */
    VER_UE4_UNDO_BREAK_MATERIALATTRIBUTES_CHANGE(352),
    /** <summary>Now Default custom profile name isn't NONE anymore due to copy/paste not working properly with it</summary> */
    VER_UE4_ADD_CUSTOMPROFILENAME_CHANGE(353),
    /** <summary>Permanently flip and scale material expression coordinates</summary> */
    VER_UE4_FLIP_MATERIAL_COORDS(354),
    /** <summary>PinSubCategoryMemberReference added to FEdGraphPinType</summary> */
    VER_UE4_MEMBERREFERENCE_IN_PINTYPE(355),
    /** <summary>Vehicles use Nm for Torque instead of cm and RPM instead of rad/s</summary> */
    VER_UE4_VEHICLES_UNIT_CHANGE(356),
    /** <summary> removes NANs from all animations when loaded now importing should detect NaNs, so we should not have NaNs in source data </summary> */
    VER_UE4_ANIMATION_REMOVE_NANS(357),
    /** <summary>Change skeleton preview attached assets property type</summary> */
    VER_UE4_SKELETON_ASSET_PROPERTY_TYPE_CHANGE(358),
    /** <summary> Fix some blueprint variables that have the CPF_DisableEditOnTemplate flag set when they shouldn't </summary> */
    VER_UE4_FIX_BLUEPRINT_VARIABLE_FLAGS(359),
    /** <summary>Vehicles use Nm for Torque instead of cm and RPM instead of rad/s part two (missed conversion for some variables</summary> */
    VER_UE4_VEHICLES_UNIT_CHANGE2(360),
    /** <summary>Changed order of interface class serialization</summary> */
    VER_UE4_UCLASS_SERIALIZE_INTERFACES_AFTER_LINKING(361),
    /** <summary>Change from LOD distances to display factors</summary> */
    VER_UE4_STATIC_MESH_SCREEN_SIZE_LODS(362),
    /** <summary>Requires test of material coords to ensure they're saved correctly</summary> */
    VER_UE4_FIX_MATERIAL_COORDS(363),
    /** <summary>Changed SpeedTree wind presets to v7</summary> */
    VER_UE4_SPEEDTREE_WIND_V7(364),
    /** <summary>NeedsLoadForEditorGame added</summary> */
    VER_UE4_LOAD_FOR_EDITOR_GAME(365),
    /** <summary>Manual serialization of FRichCurveKey to save space</summary> */
    VER_UE4_SERIALIZE_RICH_CURVE_KEY(366),
    /** <summary>Change the outer of ULandscapeMaterialInstanceConstants and Landscape-related textures to the level in which they reside</summary> */
    VER_UE4_MOVE_LANDSCAPE_MICS_AND_TEXTURES_WITHIN_LEVEL(367),
    /** <summary>FTexts have creation history data, removed Key, Namespaces, and SourceString</summary> */
    VER_UE4_FTEXT_HISTORY(368),
    /** <summary>Shift comments to the left to contain expressions properly</summary> */
    VER_UE4_FIX_MATERIAL_COMMENTS(369),
    /** <summary>Bone names stored as FName means that we can't guarantee the correct case on export, now we store a separate string for export purposes only</summary> */
    VER_UE4_STORE_BONE_EXPORT_NAMES(370),
    /** <summary>changed mesh emitter initial orientation to distribution</summary> */
    VER_UE4_MESH_EMITTER_INITIAL_ORIENTATION_DISTRIBUTION(371),
    /** <summary>Foliage on blueprints causes crashes</summary> */
    VER_UE4_DISALLOW_FOLIAGE_ON_BLUEPRINTS(372),
    /** <summary>change motors to use revolutions per second instead of rads/second</summary> */
    VER_UE4_FIXUP_MOTOR_UNITS(373),
    /** <summary>deprecated MovementComponent functions including "ModifiedMaxSpeed" et al</summary> */
    VER_UE4_DEPRECATED_MOVEMENTCOMPONENT_MODIFIED_SPEEDS(374),
    /** <summary>rename CanBeCharacterBase</summary> */
    VER_UE4_RENAME_CANBECHARACTERBASE(375),
    /** <summary>Change GameplayTagContainers to have FGameplayTags instead of FNames; Required to fix-up native serialization</summary> */
    VER_UE4_GAMEPLAY_TAG_CONTAINER_TAG_TYPE_CHANGE(376),
    /** <summary>Change from UInstancedFoliageSettings to UFoliageType, and change the api from being keyed on UStaticMesh* to UFoliageType*</summary> */
    VER_UE4_FOLIAGE_SETTINGS_TYPE(377),
    /** <summary>Lights serialize static shadow depth maps</summary> */
    VER_UE4_STATIC_SHADOW_DEPTH_MAPS(378),
    /** <summary>Add RF_Transactional to data assets, fixing undo problems when editing them</summary> */
    VER_UE4_ADD_TRANSACTIONAL_TO_DATA_ASSETS(379),
    /** <summary>Change LB_AlphaBlend to LB_WeightBlend in ELandscapeLayerBlendType</summary> */
    VER_UE4_ADD_LB_WEIGHTBLEND(380),
    /** <summary>Add root component to an foliage actor, all foliage cluster components will be attached to a root</summary> */
    VER_UE4_ADD_ROOTCOMPONENT_TO_FOLIAGEACTOR(381),
    /** <summary>FMaterialInstanceBasePropertyOverrides didn't use proper UObject serialize</summary> */
    VER_UE4_FIX_MATERIAL_PROPERTY_OVERRIDE_SERIALIZE(382),
    /** <summary>Addition of linear color sampler. color sample type is changed to linear sampler if source texture !sRGB</summary> */
    VER_UE4_ADD_LINEAR_COLOR_SAMPLER(383),
    /** <summary>Added StringAssetReferencesMap to support renames of FStringAssetReference properties.</summary> */
    VER_UE4_ADD_STRING_ASSET_REFERENCES_MAP(384),
    /** <summary>Apply scale from SCS RootComponent details in the Blueprint Editor to new actor instances at construction time</summary> */
    VER_UE4_BLUEPRINT_USE_SCS_ROOTCOMPONENT_SCALE(385),
    /** <summary>Changed level streaming to have a linear color since the visualization doesn't gamma correct.</summary> */
    VER_UE4_LEVEL_STREAMING_DRAW_COLOR_TYPE_CHANGE(386),
    /** <summary>Cleared end triggers from non-state anim notifies</summary> */
    VER_UE4_CLEAR_NOTIFY_TRIGGERS(387),
    /** <summary>Convert old curve names stored in anim assets into skeleton smartnames</summary> */
    VER_UE4_SKELETON_ADD_SMARTNAMES(388),
    /** <summary>Added the currency code field to FTextHistory_AsCurrency</summary> */
    VER_UE4_ADDED_CURRENCY_CODE_TO_FTEXT(389),
    /** <summary>Added support for C++11 enum classes</summary> */
    VER_UE4_ENUM_CLASS_SUPPORT(390),
    /** <summary>Fixup widget animation class</summary> */
    VER_UE4_FIXUP_WIDGET_ANIMATION_CLASS(391),
    /** <summary>USoundWave objects now contain details about compression scheme used.</summary> */
    VER_UE4_SOUND_COMPRESSION_TYPE_ADDED(392),
    /** <summary>Bodies will automatically weld when attached</summary> */
    VER_UE4_AUTO_WELDING(393),
    /** <summary>Rename UCharacterMovementComponent::bCrouchMovesCharacterDown</summary> */
    VER_UE4_RENAME_CROUCHMOVESCHARACTERDOWN(394),
    /** <summary>Lightmap parameters in FMeshBuildSettings</summary> */
    VER_UE4_LIGHTMAP_MESH_BUILD_SETTINGS(395),
    /** <summary>Rename SM3 to ES3_1 and updates featurelevel material node selector</summary> */
    VER_UE4_RENAME_SM3_TO_ES3_1(396),
    /** <summary>Deprecated separate style assets for use in UMG</summary> */
    VER_UE4_DEPRECATE_UMG_STYLE_ASSETS(397),
    /** <summary>Duplicating Blueprints will regenerate NodeGuids after this version</summary> */
    VER_UE4_POST_DUPLICATE_NODE_GUID(398),
    /** <summary> Rename USpringArmComponent::bUseControllerViewRotation to bUsePawnViewRotation, Rename UCameraComponent::bUseControllerViewRotation to bUsePawnViewRotation (and change the default value) </summary> */
    VER_UE4_RENAME_CAMERA_COMPONENT_VIEW_ROTATION(399),
    /** <summary>Changed FName to be case preserving</summary> */
    VER_UE4_CASE_PRESERVING_FNAME(400),
    /** <summary> Rename USpringArmComponent::bUsePawnViewRotation to bUsePawnControlRotation, Rename UCameraComponent::bUsePawnViewRotation to bUsePawnControlRotation </summary> */
    VER_UE4_RENAME_CAMERA_COMPONENT_CONTROL_ROTATION(401),
    /** <summary>Fix bad refraction material attribute masks</summary> */
    VER_UE4_FIX_REFRACTION_INPUT_MASKING(402),
    /** <summary>A global spawn rate for emitters.</summary> */
    VER_UE4_GLOBAL_EMITTER_SPAWN_RATE_SCALE(403),
    /** <summary>Cleanup destructible mesh settings</summary> */
    VER_UE4_CLEAN_DESTRUCTIBLE_SETTINGS(404),
    /** <summary>CharacterMovementComponent refactor of AdjustUpperHemisphereImpact and deprecation of some associated vars.</summary> */
    VER_UE4_CHARACTER_MOVEMENT_UPPER_IMPACT_BEHAVIOR(405),
    /** <summary>Changed Blueprint math equality functions for vectors and rotators to operate as a "nearly" equals rather than "exact"</summary> */
    VER_UE4_BP_MATH_VECTOR_EQUALITY_USES_EPSILON(406),
    /** <summary>Static lighting support was re-added to foliage, and mobility was returned to static</summary> */
    VER_UE4_FOLIAGE_STATIC_LIGHTING_SUPPORT(407),
    /** <summary>Added composite fonts to Slate font info</summary> */
    VER_UE4_SLATE_COMPOSITE_FONTS(408),
    /** <summary>Remove UDEPRECATED_SaveGameSummary, required for UWorld::Serialize</summary> */
    VER_UE4_REMOVE_SAVEGAMESUMMARY(409),
    /** <summary>Remove bodyseutp serialization from skeletal mesh component</summary> */
    VER_UE4_REMOVE_SKELETALMESH_COMPONENT_BODYSETUP_SERIALIZATION(410),
    /** <summary>Made Slate font data use bulk data to store the embedded font data</summary> */
    VER_UE4_SLATE_BULK_FONT_DATA(411),
    /** <summary>Add new friction behavior in ProjectileMovementComponent.</summary> */
    VER_UE4_ADD_PROJECTILE_FRICTION_BEHAVIOR(412),
    /** <summary>Add axis settings enum to MovementComponent.</summary> */
    VER_UE4_MOVEMENTCOMPONENT_AXIS_SETTINGS(413),
    /** <summary>Switch to new interactive comments, requires boundry conversion to preserve previous states</summary> */
    VER_UE4_GRAPH_INTERACTIVE_COMMENTBUBBLES(414),
    /** <summary>Landscape serializes physical materials for collision objects </summary> */
    VER_UE4_LANDSCAPE_SERIALIZE_PHYSICS_MATERIALS(415),
    /** <summary>Rename Visiblity on widgets to Visibility</summary> */
    VER_UE4_RENAME_WIDGET_VISIBILITY(416),
    /** <summary>add track curves for animation</summary> */
    VER_UE4_ANIMATION_ADD_TRACKCURVES(417),
    /** <summary>Removed BranchingPoints from AnimMontages and converted them to regular AnimNotifies.</summary> */
    VER_UE4_MONTAGE_BRANCHING_POINT_REMOVAL(418),
    /** <summary>Enforce const-correctness in Blueprint implementations of native C++ const class methods</summary> */
    VER_UE4_BLUEPRINT_ENFORCE_CONST_IN_FUNCTION_OVERRIDES(419),
    /** <summary>Added pivot to widget components, need to load old versions as a 0,0 pivot, new default is 0.5,0.5</summary> */
    VER_UE4_ADD_PIVOT_TO_WIDGET_COMPONENT(420),
    /** <summary>Added finer control over when AI Pawns are automatically possessed. Also renamed Pawn.AutoPossess to Pawn.AutoPossessPlayer indicate this was a setting for players and not AI.</summary> */
    VER_UE4_PAWN_AUTO_POSSESS_AI(421),
    /** <summary>Added serialization of timezone to FTextHistory for AsDate operations.</summary> */
    VER_UE4_FTEXT_HISTORY_DATE_TIMEZONE(422),
    /** <summary>Sort ActiveBoneIndices on lods so that we can avoid doing it at run time</summary> */
    VER_UE4_SORT_ACTIVE_BONE_INDICES(423),
    /** <summary>Added per-frame material uniform expressions</summary> */
    VER_UE4_PERFRAME_MATERIAL_UNIFORM_EXPRESSIONS(424),
    /** <summary>Make MikkTSpace the default tangent space calculation method for static meshes.</summary> */
    VER_UE4_MIKKTSPACE_IS_DEFAULT(425),
    /** <summary>Only applies to cooked files, grass cooking support.</summary> */
    VER_UE4_LANDSCAPE_GRASS_COOKING(426),
    /** <summary>Fixed code for using the bOrientMeshEmitters property.</summary> */
    VER_UE4_FIX_SKEL_VERT_ORIENT_MESH_PARTICLES(427),
    /** <summary>Do not change landscape section offset on load under world composition</summary> */
    VER_UE4_LANDSCAPE_STATIC_SECTION_OFFSET(428),
    /** <summary>New options for navigation data runtime generation (static, modifiers only, dynamic)</summary> */
    VER_UE4_ADD_MODIFIERS_RUNTIME_GENERATION(429),
    /** <summary>Tidied up material's handling of masked blend mode.</summary> */
    VER_UE4_MATERIAL_MASKED_BLENDMODE_TIDY(430),
    /** <summary>Original version of VER_UE4_MERGED_ADD_MODIFIERS_RUNTIME_GENERATION_TO_4_7; renumbered to prevent blocking promotion in main.</summary> */
    VER_UE4_MERGED_ADD_MODIFIERS_RUNTIME_GENERATION_TO_4_7_DEPRECATED(431),
    /** <summary>Original version of VER_UE4_AFTER_MERGED_ADD_MODIFIERS_RUNTIME_GENERATION_TO_4_7; renumbered to prevent blocking promotion in main.</summary> */
    VER_UE4_AFTER_MERGED_ADD_MODIFIERS_RUNTIME_GENERATION_TO_4_7_DEPRECATED(432),
    /** <summary>After merging VER_UE4_ADD_MODIFIERS_RUNTIME_GENERATION into 4.7 branch</summary> */
    VER_UE4_MERGED_ADD_MODIFIERS_RUNTIME_GENERATION_TO_4_7(433),
    /** <summary>After merging VER_UE4_ADD_MODIFIERS_RUNTIME_GENERATION into 4.7 branch</summary> */
    VER_UE4_AFTER_MERGING_ADD_MODIFIERS_RUNTIME_GENERATION_TO_4_7(434),
    /** <summary>Landscape grass weightmap data is now generated in the editor and serialized.</summary> */
    VER_UE4_SERIALIZE_LANDSCAPE_GRASS_DATA(435),
    /** <summary>New property to optionally prevent gpu emitters clearing existing particles on Init().</summary> */
    VER_UE4_OPTIONALLY_CLEAR_GPU_EMITTERS_ON_INIT(436),
    /** <summary>Also store the Material guid with the landscape grass data</summary> */
    VER_UE4_SERIALIZE_LANDSCAPE_GRASS_DATA_MATERIAL_GUID(437),
    /** <summary>Make sure that all template components from blueprint generated classes are flagged as public</summary> */
    VER_UE4_BLUEPRINT_GENERATED_CLASS_COMPONENT_TEMPLATES_PUBLIC(438),
    /** <summary>Split out creation method on ActorComponents to distinguish between native, instance, and simple or user construction script</summary> */
    VER_UE4_ACTOR_COMPONENT_CREATION_METHOD(439),
    /** <summary>K2Node_Event now uses FMemberReference for handling references</summary> */
    VER_UE4_K2NODE_EVENT_MEMBER_REFERENCE(440),
    /** <summary>FPropertyTag stores GUID of struct</summary> */
    VER_UE4_STRUCT_GUID_IN_PROPERTY_TAG(441),
    /** <summary>Remove unused UPolys from UModel cooked content</summary> */
    VER_UE4_REMOVE_UNUSED_UPOLYS_FROM_UMODEL(442),
    /** <summary>This doesn't do anything except trigger a rebuild on HISMC cluster trees, in this case to get a good "occlusion query" level</summary> */
    VER_UE4_REBUILD_HIERARCHICAL_INSTANCE_TREES(443),
    /** <summary>Package summary includes an CompatibleWithEngineVersion field, separately to the version it's saved with</summary> */
    VER_UE4_PACKAGE_SUMMARY_HAS_COMPATIBLE_ENGINE_VERSION(444),
    /** <summary>Track UCS modified properties on Actor Components</summary> */
    VER_UE4_TRACK_UCS_MODIFIED_PROPERTIES(445),
    /** <summary>Allowed landscape spline meshes to be stored into landscape streaming levels rather than the spline's level</summary> */
    VER_UE4_LANDSCAPE_SPLINE_CROSS_LEVEL_MESHES(446),
    /** <summary>Deprecate the variables used for sizing in the designer on UUserWidget</summary> */
    VER_UE4_DEPRECATE_USER_WIDGET_DESIGN_SIZE(447),
    /** <summary>Make the editor views array dynamically sized</summary> */
    VER_UE4_ADD_EDITOR_VIEWS(448),
    /** <summary>Updated foliage to work with either FoliageType assets or blueprint classes</summary> */
    VER_UE4_FOLIAGE_WITH_ASSET_OR_CLASS(449),
    /** <summary>Allows PhysicsSerializer to serialize shapes and actors for faster load times</summary> */
    VER_UE4_BODYINSTANCE_BINARY_SERIALIZATION(450),
    /** <summary>Added fastcall data serialization directly in UFunction</summary> */
    VER_UE4_SERIALIZE_BLUEPRINT_EVENTGRAPH_FASTCALLS_IN_UFUNCTION(451),
    /** <summary>Changes to USplineComponent and FInterpCurve</summary> */
    VER_UE4_INTERPCURVE_SUPPORTS_LOOPING(452),
    /** <summary>Material Instances overriding base material LOD transitions</summary> */
    VER_UE4_MATERIAL_INSTANCE_BASE_PROPERTY_OVERRIDES_DITHERED_LOD_TRANSITION(453),
    /** <summary>Serialize ES2 textures separately rather than overwriting the properties used on other platforms</summary> */
    VER_UE4_SERIALIZE_LANDSCAPE_ES2_TEXTURES(454),
    /** <summary>Constraint motor velocity is broken into per-component</summary> */
    VER_UE4_CONSTRAINT_INSTANCE_MOTOR_FLAGS(455),
    /** <summary>Serialize bIsConst in FEdGraphPinType</summary> */
    VER_UE4_SERIALIZE_PINTYPE_CONST(456),
    /** <summary>Change UMaterialFunction::LibraryCategories to LibraryCategoriesText (old assets were saved before auto-conversion of FArrayProperty was possible)</summary> */
    VER_UE4_LIBRARY_CATEGORIES_AS_FTEXT(457),
    /** <summary>Check for duplicate exports while saving packages.</summary> */
    VER_UE4_SKIP_DUPLICATE_EXPORTS_ON_SAVE_PACKAGE(458),
    /** <summary>Pre-gathering of gatherable, localizable text in packages to optimize text gathering operation times</summary> */
    VER_UE4_SERIALIZE_TEXT_IN_PACKAGES(459),
    /** <summary>Added pivot to widget components, need to load old versions as a 0,0 pivot, new default is 0.5,0.5</summary> */
    VER_UE4_ADD_BLEND_MODE_TO_WIDGET_COMPONENT(460),
    /** <summary>Added lightmass primitive setting</summary> */
    VER_UE4_NEW_LIGHTMASS_PRIMITIVE_SETTING(461),
    /** <summary>Deprecate NoZSpring property on spring nodes to be replaced with TranslateZ property</summary> */
    VER_UE4_REPLACE_SPRING_NOZ_PROPERTY(462),
    /** <summary>Keep enums tight and serialize their values as pairs of FName and value. Don't insert dummy values.</summary> */
    VER_UE4_TIGHTLY_PACKED_ENUMS(463),
    /** <summary>Changed Asset import data to serialize file meta data as JSON</summary> */
    VER_UE4_ASSET_IMPORT_DATA_AS_JSON(464),
    /** <summary>Legacy gamma support for textures.</summary> */
    VER_UE4_TEXTURE_LEGACY_GAMMA(465),
    /** <summary>Added WithSerializer for basic native structures like FVector, FColor etc to improve serialization performance</summary> */
    VER_UE4_ADDED_NATIVE_SERIALIZATION_FOR_IMMUTABLE_STRUCTURES(466),
    /** <summary>Deprecated attributes that override the style on UMG widgets</summary> */
    VER_UE4_DEPRECATE_UMG_STYLE_OVERRIDES(467),
    /** <summary>Shadowmap penumbra size stored</summary> */
    VER_UE4_STATIC_SHADOWMAP_PENUMBRA_SIZE(468),
    /** <summary>Fix BC on Niagara effects from the data object and dev UI changes.</summary> */
    VER_UE4_NIAGARA_DATA_OBJECT_DEV_UI_FIX(469),
    /** <summary>Fixed the default orientation of widget component so it faces down +x</summary> */
    VER_UE4_FIXED_DEFAULT_ORIENTATION_OF_WIDGET_COMPONENT(470),
    /** <summary>Removed bUsedWithUI flag from UMaterial and replaced it with a new material domain for UI</summary> */
    VER_UE4_REMOVED_MATERIAL_USED_WITH_UI_FLAG(471),
    /** <summary>Added braking friction separate from turning friction.</summary> */
    VER_UE4_CHARACTER_MOVEMENT_ADD_BRAKING_FRICTION(472),
    /** <summary>Removed TTransArrays from UModel</summary> */
    VER_UE4_BSP_UNDO_FIX(473),
    /** <summary>Added default value to dynamic parameter.</summary> */
    VER_UE4_DYNAMIC_PARAMETER_DEFAULT_VALUE(474),
    /** <summary>Added ExtendedBounds to StaticMesh</summary> */
    VER_UE4_STATIC_MESH_EXTENDED_BOUNDS(475),
    /** <summary>Added non-linear blending to anim transitions, deprecating old types</summary> */
    VER_UE4_ADDED_NON_LINEAR_TRANSITION_BLENDS(476),
    /** <summary>AO Material Mask texture</summary> */
    VER_UE4_AO_MATERIAL_MASK(477),
    /** <summary>Replaced navigation agents selection with single structure</summary> */
    VER_UE4_NAVIGATION_AGENT_SELECTOR(478),
    /** <summary>Mesh particle collisions consider particle size.</summary> */
    VER_UE4_MESH_PARTICLE_COLLISIONS_CONSIDER_PARTICLE_SIZE(479),
    /** <summary>Adjacency buffer building no longer automatically handled based on triangle count, user-controlled</summary> */
    VER_UE4_BUILD_MESH_ADJ_BUFFER_FLAG_EXPOSED(480),
    /** <summary>Change the default max angular velocity</summary> */
    VER_UE4_MAX_ANGULAR_VELOCITY_DEFAULT(481),
    /** <summary>Build Adjacency index buffer for clothing tessellation</summary> */
    VER_UE4_APEX_CLOTH_TESSELLATION(482),
    /** <summary>Added DecalSize member, solved backward compatibility</summary> */
    VER_UE4_DECAL_SIZE(483),
    /** <summary>Keep only package names in StringAssetReferencesMap</summary> */
    VER_UE4_KEEP_ONLY_PACKAGE_NAMES_IN_STRING_ASSET_REFERENCES_MAP(484),
    /** <summary>Support sound cue not saving out editor only data</summary> */
    VER_UE4_COOKED_ASSETS_IN_EDITOR_SUPPORT(485),
    /** <summary>Updated dialogue wave localization gathering logic.</summary> */
    VER_UE4_DIALOGUE_WAVE_NAMESPACE_AND_CONTEXT_CHANGES(486),
    /** <summary>Renamed MakeRot MakeRotator and rearranged parameters.</summary> */
    VER_UE4_MAKE_ROT_RENAME_AND_REORDER(487),
    /** <summary>K2Node_Variable will properly have the VariableReference Guid set if available</summary> */
    VER_UE4_K2NODE_VAR_REFERENCEGUIDS(488),
    /** <summary>Added support for sound concurrency settings structure and overrides</summary> */
    VER_UE4_SOUND_CONCURRENCY_PACKAGE(489),
    /** <summary>Changing the default value for focusable user widgets to false</summary> */
    VER_UE4_USERWIDGET_DEFAULT_FOCUSABLE_FALSE(490),
    /** <summary>Custom event nodes implicitly set 'const' on array and non-array pass-by-reference input params</summary> */
    VER_UE4_BLUEPRINT_CUSTOM_EVENT_CONST_INPUT(491),
    /** <summary>Renamed HighFrequencyGain to LowPassFilterFrequency</summary> */
    VER_UE4_USE_LOW_PASS_FILTER_FREQ(492),
    /** <summary>UAnimBlueprintGeneratedClass can be replaced by a dynamic class. Use TSubclassOf UAnimInstance instead.</summary> */
    VER_UE4_NO_ANIM_BP_CLASS_IN_GAMEPLAY_CODE(493),
    /** <summary>The SCS keeps a list of all nodes in its hierarchy rather than recursively building it each time it is requested</summary> */
    VER_UE4_SCS_STORES_ALLNODES_ARRAY(494),
    /** <summary>Moved StartRange and EndRange in UFbxAnimSequenceImportData to use FInt32Interval</summary> */
    VER_UE4_FBX_IMPORT_DATA_RANGE_ENCAPSULATION(495),
    /** <summary>Adding a new root scene component to camera component</summary> */
    VER_UE4_CAMERA_COMPONENT_ATTACH_TO_ROOT(496),
    /** <summary>Updating custom material expression nodes for instanced stereo implementation</summary> */
    VER_UE4_INSTANCED_STEREO_UNIFORM_UPDATE(497),
    /** <summary>Texture streaming min and max distance to handle HLOD</summary> */
    VER_UE4_STREAMABLE_TEXTURE_MIN_MAX_DISTANCE(498),
    /** <summary>Fixing up invalid struct-to-struct pin connections by injecting available conversion nodes</summary> */
    VER_UE4_INJECT_BLUEPRINT_STRUCT_PIN_CONVERSION_NODES(499),
    /** <summary>Saving tag data for Array Property's inner property</summary> */
    VER_UE4_INNER_ARRAY_TAG_INFO(500),
    /** <summary>Fixed duplicating slot node names in skeleton due to skeleton preload on compile</summary> */
    VER_UE4_FIX_SLOT_NAME_DUPLICATION(501),
    /** <summary>Texture streaming using AABBs instead of Spheres</summary> */
    VER_UE4_STREAMABLE_TEXTURE_AABB(502),
    /** <summary>FPropertyTag stores GUID of property</summary> */
    VER_UE4_PROPERTY_GUID_IN_PROPERTY_TAG(503),
    /** <summary>Name table hashes are calculated and saved out rather than at load time</summary> */
    VER_UE4_NAME_HASHES_SERIALIZED(504),
    /** <summary>Updating custom material expression nodes for instanced stereo implementation refactor</summary> */
    VER_UE4_INSTANCED_STEREO_UNIFORM_REFACTOR(505),
    /** <summary>Added compression to the shader resource for memory savings</summary> */
    VER_UE4_COMPRESSED_SHADER_RESOURCES(506),
    /** <summary>Cooked files contain the dependency graph for the event driven loader (the serialization is largely independent of the use of the new loader)</summary> */
    VER_UE4_PRELOAD_DEPENDENCIES_IN_COOKED_EXPORTS(507),
    /** <summary>Cooked files contain the TemplateIndex used by the event driven loader (the serialization is largely independent of the use of the new loader, i.e. this will be null if cooking for the old loader)</summary> */
    VER_UE4_TemplateIndex_IN_COOKED_EXPORTS(508),
    /** <summary>FPropertyTag includes contained type(s) for Set and Map properties</summary> */
    VER_UE4_PROPERTY_TAG_SET_MAP_SUPPORT(509),
    /** <summary>Added SearchableNames to the package summary and asset registry</summary> */
    VER_UE4_ADDED_SEARCHABLE_NAMES(510),
    /** <summary>Increased size of SerialSize and SerialOffset in export map entries to 64 bit, allow support for bigger files</summary> */
    VER_UE4_64BIT_EXPORTMAP_SERIALSIZES(511),
    /** <summary>Sky light stores IrradianceMap for mobile renderer.</summary> */
    VER_UE4_SKYLIGHT_MOBILE_IRRADIANCE_MAP(512),
    /** <summary>Added flag to control sweep behavior while walking in UCharacterMovementComponent.</summary> */
    VER_UE4_ADDED_SWEEP_WHILE_WALKING_FLAG(513),
    /** <summary>StringAssetReference changed to SoftObjectPath and swapped to serialize as a name+string instead of a string</summary> */
    VER_UE4_ADDED_SOFT_OBJECT_PATH(514),
    /** <summary>Changed the source orientation of point lights to match spot lights (z axis)</summary> */
    VER_UE4_POINTLIGHT_SOURCE_ORIENTATION(515),
    /** <summary>LocalizationId has been added to the package summary (editor-only)</summary> */
    VER_UE4_ADDED_PACKAGE_SUMMARY_LOCALIZATION_ID(516),
    /** <summary>Fixed case insensitive hashes of wide strings containing character values from 128-255</summary> */
    VER_UE4_FIX_WIDE_STRING_CRC(517),
    /** <summary>Added package owner to allow private references</summary> */
    VER_UE4_ADDED_PACKAGE_OWNER(518),
    /** <summary>Changed the data layout for skin weight profile data</summary> */
    VER_UE4_SKINWEIGHT_PROFILE_DATA_LAYOUT_CHANGES(519),
    /** <summary>Added import that can have package different than their outer</summary> */
    VER_UE4_NON_OUTER_PACKAGE_IMPORT(520),
    /** <summary>Added DependencyFlags to AssetRegistry</summary> */
    VER_UE4_ASSETREGISTRY_DEPENDENCYFLAGS(521),
    /** <summary>Fixed corrupt licensee flag in 4.26 assets</summary> */
    VER_UE4_CORRECT_LICENSEE_FLAG(522),
    VER_UE4_AUTOMATIC_VERSION_PLUS_ONE(523),
    /** <summary>The newest specified version of UE4.</summary> */
    VER_UE4_AUTOMATIC_VERSION(VER_UE4_AUTOMATIC_VERSION_PLUS_ONE.value - 1);
}

enum class ObjectVersionUE5(val value: Int) {
    /** <summary>Unknown version</summary> */
    UNKNOWN(0),
    /** <summary>The original UE5 version, at the time this was added the UE4 version was 522, so UE5 will start from 1000 to show a clear difference</summary> */
    INITIAL_VERSION(1000),
    /** <summary>Support stripping names that are not referenced from export data</summary> */
    NAMES_REFERENCED_FROM_EXPORT_DATA(1001),
    /** <summary>Added a payload table of contents to the package summary </summary> */
    PAYLOAD_TOC(1002),
    /** <summary>Added data to identify references from and to optional package</summary> */
    OPTIONAL_RESOURCES(1003),
    /** <summary>Large world coordinates converts a number of core types to double components by default.</summary> */
    LARGE_WORLD_COORDINATES(1004),
    /** <summary>Remove package GUID from FObjectExport</summary> */
    REMOVE_OBJECT_EXPORT_PACKAGE_GUID(1005),
    /** <summary>Add IsInherited to the FObjectExport entry</summary> */
    TRACK_OBJECT_EXPORT_IS_INHERITED(1006),
    /** <summary>Replace FName asset path in FSoftObjectPath with (package name, asset name) pair FTopLevelAssetPath</summary> */
    FSOFTOBJECTPATH_REMOVE_ASSET_PATH_FNAMES(1007),
    /** <summary>Add a soft object path list to the package summary for fast remap</summary> */
    ADD_SOFTOBJECTPATH_LIST(1008),
    /** <summary>Added bulk/data resource table</summary> */
    DATA_RESOURCES(1009),
    /** <summary>Added script property serialization offset to export table entries for saved, versioned packages</summary> */
    SCRIPT_SERIALIZATION_OFFSET(1010),
    /** <summary> Adding property tag extension, Support for overridable serialization on UObject, Support for overridable logic in containers </summary> */
    PROPERTY_TAG_EXTENSION_AND_OVERRIDABLE_SERIALIZATION(1011),
    /** <summary>Added property tag complete type name and serialization type</summary> */
    PROPERTY_TAG_COMPLETE_TYPE_NAME(1012),
    /** <summary>Changed UE::AssetRegistry::WritePackageData to include PackageBuildDependencies</summary> */
    ASSETREGISTRY_PACKAGEBUILDDEPENDENCIES(1013),
    /** <summary>Added meta data serialization offset to for saved, versioned packages</summary> */
    METADATA_SERIALIZATION_OFFSET(1014),
    /** <summary>Added VCells to the object graph</summary> */
    VERSE_CELLS(1015),
    /** <summary>Changed PackageFileSummary to write FIoHash PackageSavedHash instead of FGuid Guid</summary> */
    PACKAGE_SAVED_HASH(1016),
    /** <summary>OS shadow serialization of subobjects</summary> */
    OS_SUB_OBJECT_SHADOW_SERIALIZATION(1017),
    /** <summary>Adds a table of hierarchical type information for imports in a package</summary> */
    IMPORT_TYPE_HIERARCHIES(1018),
    AUTOMATIC_VERSION_PLUS_ONE(1019),
    /** <summary>The newest specified version of UE5.</summary> */
    AUTOMATIC_VERSION(AUTOMATIC_VERSION_PLUS_ONE.value - 1);
}

enum class GameSpecificOverride(val value: Int) {
    None(0);
}
