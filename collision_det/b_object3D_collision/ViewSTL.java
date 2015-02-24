package b_object3D_collision;

import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;

import b_object3D_collision.Triangle3D.Pair3D;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;

import org.fxyz.shapes.composites.PolyLine3D;



// This code is originally based on code from:
// http://stackoverflow.com/questions/19462571/how-to-create-3d-shape-from-stl-in-javafx-8

// The current (1/14/2015) architecture is a kind of a View-Controller. I did not separate 
//   out Model from View because the Model information is stored in MeshView class that is 
//   part of the View. However, any changes to the Model are then immediately reflected in 
//   the view. It probably makes sense to just put MeshView in Model and and treat it 
//   simply as such. (Interestingly, then, model does not even need to notify view in case 
//   of a change. The notice happens behind the scenes; no pun intended. That type of 
//   functionality is likely intentional in JavaFX.) At present I am leaving taking model 
//   out of view for later.  
//   

public class ViewSTL {

  private Controller controller;

  private static final String MESH_DIRECTORY =
    "C:/Users/Vinushka/Documents/school/top_algorithms/topics_algorithms_ncf_2015/compression/stlFiles";

  private static final double MODEL_SCALE_FACTOR = 4;
  private static final double MODEL_X_OFFSET = 0; // standard
  private static final double MODEL_Y_OFFSET = 0; // standard

  private static final int VIEWPORT_SIZE = 800;

  private static final Color   lightColor = Color.rgb(244, 255, 250);//Color.rgb(  0,   0,   0);//
  private static final Color  lightDColor = Color.rgb( 80,  80,  80);//Color.rgb(  0,   0,   0);//Darker version of color//
  private static final Color ambientColor = Color.rgb( 80,  80,  80,   0);
  private static final Color  objectColor = Color.rgb(180, 170, 122);//Color.rgb(  0,   0,   0);//
  private static final Color objectDColor = Color.rgb( 60,  60,  40);//Color.rgb(  0,   0,   0);//Darker version of color//
  private static final Color  collsnColor = Color.rgb(255,  20,  20);
  private static final Color   sceneColor = Color.rgb( 10,  10,  40);

  private Stage primaryStage;
  private Group path3D;
  private Group rootAll;
  private Group rootMeshAll; // Contains all/only displayed meshes, used only for translation
  private Group rootMeshRotates; // Contains all/only displayed meshes, used only for rotation
  private   PointLight   pointLight1;
  private   PointLight   pointLight2;
  private   PointLight   pointLight3;
  private AmbientLight ambientLight ;

  ViewSTL() {
      rootMeshRotates = new Group();
      path3D          = new Group();
  }

  ViewSTL(Controller controller) {
      this();
      this.controller = controller;
      this.controller.setView(this);
  }

  // Initial scene layout
  MeshView loadMeshView() {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setInitialDirectory( new File(MESH_DIRECTORY) );
      fileChooser.setTitle("Open Resource File");
      fileChooser.getExtensionFilters().addAll(
              new ExtensionFilter("Stereolithography Files", "*.stl"),
              new ExtensionFilter("All Files", "*.*"));
      File selectedFile = fileChooser.showOpenDialog(primaryStage);
      if (selectedFile != null) {
            StlMeshImporter importer = new StlMeshImporter();
            importer.read(selectedFile);
            TriangleMesh mesh = (TriangleMesh)importer.getImport();
            System.out.println("Imported file: "+selectedFile+" With, "+mesh.getFaces().size()/mesh.getFaceElementSize()+" facets!");
            MeshView meshView = new MeshView(mesh);
            
            int nFiles = rootMeshRotates.getChildren().size(); 
            meshView.setTranslateX(VIEWPORT_SIZE / 4 * nFiles - VIEWPORT_SIZE / 6);
            //meshView.setTranslateY(VIEWPORT_SIZE / 2 + MODEL_Y_OFFSET);
            //meshView.setTranslateZ(VIEWPORT_SIZE / 2);
            meshView.setScaleX(MODEL_SCALE_FACTOR);
            meshView.setScaleY(MODEL_SCALE_FACTOR);
            meshView.setScaleZ(MODEL_SCALE_FACTOR);
            
            PhongMaterial sample = new PhongMaterial(objectColor);
            sample.setSpecularColor(lightColor);
            sample.setSpecularPower(16);
            meshView.setMaterial(sample);
            
            meshView.setOnMouseEntered(  e -> {controller.notifyMouseEnteredMesh( e,meshView);} );
            
            rootMeshRotates.getChildren().add( meshView );
            
            return meshView;
      }
      return null;
  }

  void removeMeshView( MeshView meshView ) {
      rootMeshRotates.getChildren().remove( meshView );
  }

  private Group buildScene() {
    rootMeshAll     = new Group(rootMeshRotates);
    rootAll         = new Group(rootMeshAll, path3D);
    rootMeshAll.setTranslateX(   VIEWPORT_SIZE  /2 + MODEL_X_OFFSET);
    rootMeshAll.setTranslateY(   VIEWPORT_SIZE  /2 + MODEL_Y_OFFSET);
    rootMeshAll.setTranslateZ(   VIEWPORT_SIZE  /2);

    pointLight1 = new PointLight(lightColor);
    pointLight1.setTranslateX(   VIEWPORT_SIZE*3/4);
    pointLight1.setTranslateY(   VIEWPORT_SIZE  /2);
    pointLight1.setTranslateZ(   VIEWPORT_SIZE  /2);
    pointLight2 = new PointLight(lightColor);
    pointLight2.setTranslateX(   VIEWPORT_SIZE*1/4);
    pointLight2.setTranslateY(   VIEWPORT_SIZE*3/4);
    pointLight2.setTranslateZ(   VIEWPORT_SIZE*3/4);
    pointLight3 = new PointLight(lightColor);
    pointLight3.setTranslateX(   VIEWPORT_SIZE*5/8);
    pointLight3.setTranslateY(   VIEWPORT_SIZE  /2);
    pointLight3.setTranslateZ(  -VIEWPORT_SIZE    );

    ambientLight = new AmbientLight(ambientColor);

    rootAll.getChildren().add(  pointLight1);
    rootAll.getChildren().add(  pointLight2);
    rootAll.getChildren().add(  pointLight3);
    rootAll.getChildren().add(ambientLight );

    rootAll.setOnMouseEntered(  e -> {controller.notifyMouseEntered( e,rootAll);});
    rootAll.setOnMouseExited(   e -> {controller.notifyMouseExited(  e,rootAll);});

    return rootAll;
  }

  private PerspectiveCamera addCamera(SubScene scene) {
    PerspectiveCamera perspectiveCamera = new PerspectiveCamera();
    System.out.println("Near Clip: " + perspectiveCamera.getNearClip());
    System.out.println("Far Clip:  " + perspectiveCamera.getFarClip());
    System.out.println("FOV:       " + perspectiveCamera.getFieldOfView());

    scene.setCamera(perspectiveCamera);
    return perspectiveCamera;
  }

  public void start(Stage primaryStage) {
    this.primaryStage = primaryStage;
    AnchorPane pane = new AnchorPane();
    
    HBox hBox = new HBox(5); // 5 adds that space between nodes
    ToggleButton    wireFrameButton = new ToggleButton("Wireframe");
    wireFrameButton   .setOnAction( e -> {controller.notifyWireFrameButton(  e );});
    ToggleButton   crossSectnButton = new ToggleButton("Cross-section");
    crossSectnButton  .setOnAction( e -> {controller.notifyCrossSectnButton( e );});
    ToggleButton   renderBackButton = new ToggleButton("Render Backs");
    renderBackButton  .setOnAction( e -> {controller.notifyRenderBackButton( e );});
    ToggleButton   checkCllsnButton = new ToggleButton("Check Collision");
    checkCllsnButton  .setOnAction( e -> {controller.notifyCheckCllsnButton( e );});
    Button         addSTLFileButton = new Button("Add File");
    addSTLFileButton  .setOnAction( e -> {controller.notifyAddSTLFileButton( e );});
    Button         removeFileButton = new Button("Remove");
    removeFileButton  .setOnAction( e -> {controller.notifyRemoveFileButton( e );});
    ToggleButton badCollisionAlgButton = new ToggleButton("Check Collision (slow)");
    badCollisionAlgButton  .setOnAction( e -> {controller.notifySlowCollisionButton( e );});
    hBox.getChildren().addAll(
            wireFrameButton,crossSectnButton,renderBackButton,
            checkCllsnButton,addSTLFileButton,removeFileButton,badCollisionAlgButton);
    AnchorPane.setLeftAnchor(hBox, 10.0);
    AnchorPane.setTopAnchor( hBox, 10.0);

    Group meshDisplay = buildScene();
    meshDisplay.setScaleX(2);
    meshDisplay.setScaleY(2);
    meshDisplay.setScaleZ(2);
    meshDisplay.setTranslateX(50);
    meshDisplay.setTranslateY(50);

    Scene       scene = new Scene(          pane, VIEWPORT_SIZE, VIEWPORT_SIZE);//, true);//
    SubScene subScene = new SubScene(meshDisplay, VIEWPORT_SIZE, VIEWPORT_SIZE, true, SceneAntialiasing.BALANCED);//, true, SceneAntialiasing.DISABLED);//);//
    subScene.setFill(sceneColor);
    addCamera(subScene);
    pane.getChildren().add(subScene);
    pane.getChildren().add(  hBox      );
    scene.setOnKeyPressed(  (KeyEvent e) -> {controller.notifyKeyPressed( e );} );
    scene.setOnMousePressed(  e -> {controller.notifyMousePressed( e,rootMeshRotates);});
    scene.setOnMouseReleased( e -> {controller.notifyMouseReleased(e,rootMeshRotates);});
    scene.setOnMouseDragged(  e -> {controller.notifyMouseDragged( e,rootMeshRotates);});
    scene.setOnScroll(        e -> {controller.notifyScrollMove(   e                );});

    primaryStage.setTitle("STL Object Viewer");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  // Transformations applied later in the execution
  void rotate(double angle, Point3D rotationAxis, boolean isCrossSection) {
      Rotate rotate = new Rotate(angle,rotationAxis);
      rootMeshRotates.getTransforms().addAll( rotate );
      path3D.getChildren().clear();
      if (isCrossSection) crossSection();
  }
  
  void translateAllMeshes(double mult) {
    rootMeshAll.getTransforms().add(new Translate(0,0,mult * VIEWPORT_SIZE));
  }
  
  void setRender( boolean isWireframe ) {
    for (int i = 0; i < rootMeshRotates.getChildren().size(); i++) {
        if (isWireframe) {
          ((Shape3D)rootMeshRotates.getChildren().get(i)).drawModeProperty().set(DrawMode.LINE);
        } else {
          ((Shape3D)rootMeshRotates.getChildren().get(i)).drawModeProperty().set(DrawMode.FILL);
        }
    }
  }
  
  void setBackFace( boolean isDisplayBack ) {
    for (int i = 0; i < rootMeshRotates.getChildren().size(); i++) {
        if (isDisplayBack) {
          ((Shape3D)rootMeshRotates.getChildren().get(i)).cullFaceProperty().set(CullFace.NONE);//
        } else {
          ((Shape3D)rootMeshRotates.getChildren().get(i)).cullFaceProperty().set(CullFace.BACK);//
        }
    }
  }
  
  void setColorsNormal(  ) {
      path3D.getChildren().clear();
      PhongMaterial sample = new PhongMaterial(objectColor);
      sample.setSpecularColor(lightColor);
      sample.setSpecularPower(16);
      for (int i = 0; i < rootMeshRotates.getChildren().size(); i++) {
          ((Shape3D)rootMeshRotates.getChildren().get(i)).setMaterial(sample);;//
      }
    }
        
  void setColorsDarker(  ) {
      PhongMaterial sample = new PhongMaterial(objectDColor);
      sample.setSpecularColor(lightDColor);
      sample.setSpecularPower(16);
      for (int i = 0; i < rootMeshRotates.getChildren().size(); i++) {
          ((Shape3D)rootMeshRotates.getChildren().get(i)).setMaterial(sample);;//
      }
    }

  //first set up the functions we need for the collision detection below
  
  ArrayList<Triangle3D> getAllTrianglesFromObject(int iA){
	  ArrayList<Triangle3D> object = new ArrayList<Triangle3D>();
	  MeshView shapeA = (MeshView)rootMeshRotates.getChildren().get(iA);
	  TriangleMesh meshA = (TriangleMesh)shapeA.getMesh();
	  for (     int iFaceA = 0; iFaceA < meshA.getFaces().size()/meshA.getFaceElementSize(); iFaceA++ ) {
		  int iFaceA0 = meshA.getFaces().get(iFaceA*6+0); // i*6+
          int iFaceA1 = meshA.getFaces().get(iFaceA*6+2);
          int iFaceA2 = meshA.getFaces().get(iFaceA*6+4);
          Point3D a0 = shapeA.localToScene( meshA.getPoints().get(iFaceA0*3+0), 
        		  meshA.getPoints().get(iFaceA0*3+1), 
        		  meshA.getPoints().get(iFaceA0*3+2) );
          Point3D a1 = shapeA.localToScene( meshA.getPoints().get(iFaceA1*3+0), 
        		  meshA.getPoints().get(iFaceA1*3+1), 
        		  meshA.getPoints().get(iFaceA1*3+2) );
          Point3D a2 = shapeA.localToScene( meshA.getPoints().get(iFaceA2*3+0), 
        		  meshA.getPoints().get(iFaceA2*3+1), 
        		  meshA.getPoints().get(iFaceA2*3+2) );
          Triangle3D a = new Triangle3D( a0, a1, a2 );
          object.add(a);
	  }
	  return object;
  }
  
  float[] getBoundingBox(ArrayList<Triangle3D> object, float[] oldBox){
	  //returns an array with the bounding box defined by the min and max of the arraylist of triangles
	  //this fails if our coordinates are weirdly rotated, but I think this should be fine overall
	  //oldBox is an optional argument, we use it if we have to define a different box.
	  float minX, maxX, minY, maxY, minZ, maxZ; //declare all 6 vars at the top
	  if(oldBox != null){
		  minX = oldBox[0];maxX=oldBox[1];minY=oldBox[2];maxY=oldBox[3];minZ=oldBox[4];maxZ=oldBox[5];
	  }else{
		  //	declare all 6 to be 0 for now
		  minX = maxX = minY = maxY = minZ = maxZ = 0;
	  }
	  for(Triangle3D t : object){
		  //loop through, get min and max
		  //each triangle3d object has 3 float arrays with the points...
		  //we just hardcode those in here so we don't have to change the code over there
		  if(minX == 0){
			  //nothing is 0 in an STL file as far as I know so we can just take the point in
			  minX = maxX = t.a[0];
			  minY = maxY = t.a[1];
			  minZ = maxZ = t.a[2];
		  }else{
			  //I can't think of a shorter way to write this other than to offload the functionality
			  //to the Triangle3D class, but then I'd just be calling getMinPoints() or something similar
			  //which would execute this very same block, but in Triangle3D instead of here
			  //I don't think the object orientation is worthwhile in algorithmic terms here
			  if(t.a[0] < minX) minX = t.a[0];
			  if(t.a[0] > maxX) maxX = t.a[0];
			  if(t.a[1] < minY) minY = t.a[1];
			  if(t.a[1] > maxY) maxY = t.a[1];
			  if(t.a[2] < minZ) minZ = t.a[2];
			  if(t.a[2] > maxZ) maxZ = t.a[2];
			  if(t.b[0] < minX) minX = t.b[0];
			  if(t.b[0] > maxX) maxX = t.b[0];
			  if(t.b[1] < minY) minY = t.b[1];
			  if(t.b[1] > maxY) maxY = t.b[1];
			  if(t.b[2] < minZ) minZ = t.b[2];
			  if(t.b[2] > maxZ) maxZ = t.b[2];
			  if(t.c[0] < minX) minX = t.c[0];
			  if(t.c[0] > maxX) maxX = t.c[0];
			  if(t.c[1] < minY) minY = t.c[1];
			  if(t.c[1] > maxY) maxY = t.c[1];
			  if(t.c[2] < minZ) minZ = t.c[2];
			  if(t.c[2] > maxZ) maxZ = t.c[2];
		  } 
	  }
	  return new float[]{minX,maxX,minY,maxY,minZ,maxZ};
  }
  
  
  private ArrayList<Triangle3D> getTrianglesInBox(
		ArrayList<Triangle3D> triangles, float minX, float maxX, float minY,
		float maxY, float minZ, float maxZ) {
	//Once again we have the same problem as getBoundingBox
	//so we just take the same approach here as we did there
	  //From a design perspective, this method is really ugly
	  //Howewver I can't come up with a way to do this without adding too much complexity
	  ArrayList<Triangle3D> output_objects = new ArrayList<Triangle3D>();
	  for(Triangle3D object : triangles){
//		  boolean is_inside_box = object.isCollisionBoundingBoxExhaustive(new float[]{minX,minY,minZ}, new float[]{maxX,maxY,maxZ});
		  float ax = object.a[0];float ay = object.a[1];float az = object.a[2];float bx = object.b[0];float by = object.b[1];
		  float bz = object.b[2];float cx = object.c[0];float cy = object.c[1];float cz = object.c[2];
          boolean not_inside_box = 
                  !((ax >= minX && ax <= maxX && ay >= minY && ay <= maxY && az >= minZ && az <= maxZ) ||
                   (bx >= minX && bx <= maxX && by >= minY && by <= maxY && bz >= minZ && bz <= maxZ) ||
                   (cx >= minX && cx <= maxX && cy >= minY && cy <= maxY && cz >= minZ && cz <= maxZ)); //first see if all points are in the box
          boolean clearly_outside_box = 
                  (ax <= minX && bx <= minX && cx <= minX) || (ax >= maxX && bx >= maxX && cx >= maxX) ||
                  (ay <= minY && by <= minY && cy <= minY) || (ay >= maxY && by >= maxY && cy >= maxY) ||
                  (az <= minZ && bz <= minZ && cz <= minZ) || (az >= maxZ && bz >= maxZ && cz >= maxZ)   ; //first see if all points are in the box
		  //make a separate if since we need to do the above check before we move on
          //if((!(not_inside_box) || edge_through_box) && !(clearly_outside_box)){ //if it's not outside the box, then it must be in the box, so add it.
          if(!(not_inside_box && clearly_outside_box)){ //if it's not outside the box, then it must be in the box, so add it.
          //if(!(not_inside_box) || edge_through_box){ //if it's not outside the box, then it must be in the box, so add it.
			  output_objects.add(object);
		  }
	  }
	return output_objects;
}
  
  boolean isCollision(boolean slow) {
      setColorsNormal();
      boolean returnValue = false;
      //before we run this, we need to find two things
      //One, the bounds of the box that holds the triangles
      //Two, get a list of all the triangles from objectA and
      //from objectB
      //To define the boundingbox, instead of relying on the Java application
      //which I don't think I can quite trust, I will get the min and max X, Y, Z
      //from the set of all triangles.  This way I can make a regular box that does
      //not depend on rotation axes or anything that could mess up my algorithm
      //for checking if a triangle is inside a box.
      //To define that box, I need to iterate through all triangles from both objectA
      //and from objectB
      //first take code from the old isCollision to get everything as Triangle3D objects
      //that way we can at the end of everything just call Triangle3D's isCollision method
      //between triangle pairs until we're done, skipping over the old method entirely.
      System.out.println("Checking collision...");
//		PrintStream originalSerr = System.err;
//		System.setErr(new PrintStream(new OutputStream(){
//			@Override
//			public void write(int b) {} //empty prints so we save on writing to console
//		}));
		//kill stderr for a while so the collision checker doesn't go nuts
      for     (int iA =    0; iA < rootMeshRotates.getChildren().size() - 1; iA++) { 
    	  //loop over (n-1) objects so that you don't do an array-fill loop when you don't need to
    	  //now get all the triangles from object A
    	  ArrayList<Triangle3D> objectA = getAllTrianglesFromObject(iA);
    	  float[] box = getBoundingBox(objectA,null); //since we want to get a new box at this point
          for (int iB = iA+1; iB < rootMeshRotates.getChildren().size(); iB++) {
        	  if(slow){
        		  System.out.println("Slow collision...");
        		  returnValue = isCollisionOld(iA,iB);
        	  }else{

        		  ArrayList<Triangle3D> objectB = getAllTrianglesFromObject(iB);
        		  //now define the bounding box
        		  box = getBoundingBox(objectB,box); //make sure we take in the old box to get a proper bounding box that bounds both of them
        		  //now call isCollisionRecursive
        		  //        	  System.out.println("In isCollision()");
        		  returnValue = isCollisionRecursive(objectA,objectB,box[0],box[1],box[2],box[3],box[4],box[5], iA, iB, 0);
        		  //        	  System.out.println("Collision is: " + returnValue);

        		  //if ( isCollision(iA,iB) ) returnValue = true;
        	  }
          }
      }
//      System.setErr(originalSerr);
      return returnValue;
  }
  
  
  boolean isCollisionOld(int iA, int iB) {
      MeshView shapeA = (MeshView)rootMeshRotates.getChildren().get(iA);
      MeshView shapeB = (MeshView)rootMeshRotates.getChildren().get(iB);
      
      TriangleMesh meshA = (TriangleMesh)shapeA.getMesh();
      TriangleMesh meshB = (TriangleMesh)shapeB.getMesh();

      for (     int iFaceA = 0; iFaceA < meshA.getFaces().size()/meshA.getFaceElementSize(); iFaceA++ ) {
          for ( int iFaceB = 0; iFaceB < meshB.getFaces().size()/meshB.getFaceElementSize(); iFaceB++ ) {
              int iFaceA0 = meshA.getFaces().get(iFaceA*6+0); // i*6+
              int iFaceA1 = meshA.getFaces().get(iFaceA*6+2);
              int iFaceA2 = meshA.getFaces().get(iFaceA*6+4);
              int iFaceB0 = meshB.getFaces().get(iFaceB*6+0);
              int iFaceB1 = meshB.getFaces().get(iFaceB*6+2);
              int iFaceB2 = meshB.getFaces().get(iFaceB*6+4);

              Point3D a0 = shapeA.localToScene( meshA.getPoints().get(iFaceA0*3+0), 
                                                  meshA.getPoints().get(iFaceA0*3+1), 
                                                  meshA.getPoints().get(iFaceA0*3+2) );
              Point3D a1 = shapeA.localToScene( meshA.getPoints().get(iFaceA1*3+0), 
                                                meshA.getPoints().get(iFaceA1*3+1), 
                                                meshA.getPoints().get(iFaceA1*3+2) );
              Point3D a2 = shapeA.localToScene( meshA.getPoints().get(iFaceA2*3+0), 
                                                meshA.getPoints().get(iFaceA2*3+1), 
                                                meshA.getPoints().get(iFaceA2*3+2) );
              Triangle3D a = new Triangle3D( a0, a1, a2 );

              Point3D b0 = shapeB.localToScene( meshB.getPoints().get(iFaceB0*3+0), 
                                                meshB.getPoints().get(iFaceB0*3+1), 
                                                meshB.getPoints().get(iFaceB0*3+2) );
              Point3D b1 = shapeB.localToScene( meshB.getPoints().get(iFaceB1*3+0), 
                                                meshB.getPoints().get(iFaceB1*3+1), 
                                                meshB.getPoints().get(iFaceB1*3+2) );
              Point3D b2 = shapeB.localToScene( meshB.getPoints().get(iFaceB2*3+0), 
                                                meshB.getPoints().get(iFaceB2*3+1), 
                                                meshB.getPoints().get(iFaceB2*3+2) );
              Triangle3D b = new Triangle3D( b0, b1, b2 );

              //System.out.println("triangle: "+a.ax+" "+a.ay+" "+a.az+" "+b.ax+" "+b.ay+" "+b.az+" ");

              // TODO Remove this before giving to students (or ask them if it is needed)
              {
                  float minAx, minAy, minAz, minBx, minBy, minBz;
                  float maxAx, maxAy, maxAz, maxBx, maxBy, maxBz;

                  if (a0.getX()>a1.getX()) { minAx = (float) a1.getX(); maxAx = (float) a0.getX(); } else { minAx = (float) a0.getX(); maxAx = (float) a1.getX(); } if (a2.getX()<minAx) { minAx = (float) a2.getX(); } if (a2.getX()>maxAx) { maxAx = (float) a2.getX(); } 
                  if (b0.getX()>b1.getX()) { minBx = (float) b1.getX(); maxBx = (float) b0.getX(); } else { minBx = (float) b0.getX(); maxBx = (float) b1.getX(); } if (b2.getX()<minBx) { minBx = (float) b2.getX(); } if (b2.getX()>maxBx) { maxBx = (float) b2.getX(); } if (minAx>maxBx || maxAx<minBx) continue;
                  if (a0.getY()>a1.getY()) { minAy = (float) a1.getY(); maxAy = (float) a0.getY(); } else { minAy = (float) a0.getY(); maxAy = (float) a1.getY(); } if (a2.getY()<minAy) { minAy = (float) a2.getY(); } if (a2.getY()>maxAy) { maxAy = (float) a2.getY(); } 
                  if (b0.getY()>b1.getY()) { minBy = (float) b1.getY(); maxBy = (float) b0.getY(); } else { minBy = (float) b0.getY(); maxBy = (float) b1.getY(); } if (b2.getY()<minBy) { minBy = (float) b2.getY(); } if (b2.getY()>maxBy) { maxBy = (float) b2.getY(); } if (minAy>maxBy || maxAy<minBy) continue; 
                  if (a0.getZ()>a1.getZ()) { minAz = (float) a1.getZ(); maxAz = (float) a0.getZ(); } else { minAz = (float) a0.getZ(); maxAz = (float) a1.getZ(); } if (a2.getZ()<minAz) { minAz = (float) a2.getZ(); } if (a2.getZ()>maxAz) { maxAz = (float) a2.getZ(); } 
                  if (b0.getZ()>b1.getZ()) { minBz = (float) b1.getZ(); maxBz = (float) b0.getZ(); } else { minBz = (float) b0.getZ(); maxBz = (float) b1.getZ(); } if (b2.getZ()<minBz) { minBz = (float) b2.getZ(); } if (b2.getZ()>maxBz) { maxBz = (float) b2.getZ(); } if (minAz>maxBz || maxAz<minBz) continue; 
              }

              if ( a.isCollision(b) ) {
                  System.out.println("COLLISION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                  System.out.println("Triangles are at: [" + a.a[0] + "," + a.a[1] + "," + a.a[2] + "]" +
                		  "[" + a.b[0] + "," + a.b[1] + "," + a.b[2] + "]" +
                		  "[" + a.c[0] + "," + a.c[1] + "," + a.c[2] + "]" +
                		  " and " + 
                		  "[" + b.a[0] + "," + b.a[1] + "," + b.a[2] + "]" +
                		  "[" + b.b[0] + "," + b.b[1] + "," + b.b[2] + "]" +
                		  "[" + b.c[0] + "," + b.c[1] + "," + b.c[2] + "]" + ""
                		  );
                  PhongMaterial sample = new PhongMaterial(collsnColor);
                  sample.setSpecularColor(lightColor);
                  sample.setSpecularPower(16);
                  shapeA.setMaterial(sample);
                  shapeB.setMaterial(sample);
                  return true;
              }
          }
      }

      return false;
  }
  
  
 
  
  boolean isCollision(ArrayList<Triangle3D> objectA, ArrayList<Triangle3D> objectB, int iA, int iB) {
	  //we need iA and iB purely to make the objects red
	  MeshView shapeA = (MeshView)rootMeshRotates.getChildren().get(iA);
	  MeshView shapeB = (MeshView)rootMeshRotates.getChildren().get(iB);
	  //after this we don't need it
      for (     int iFaceA = 0; iFaceA < objectA.size(); iFaceA++ ) {
    	  Triangle3D a = objectA.get(iFaceA);
          for ( int iFaceB = 0; iFaceB < objectB.size(); iFaceB++ ) {
              Triangle3D b = objectB.get(iFaceB);
              if ( a.isCollision(b) ) {
                  System.out.println("COLLISION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                  PhongMaterial sample = new PhongMaterial(collsnColor);
                  sample.setSpecularColor(lightColor);
                  sample.setSpecularPower(16);
                  shapeA.setMaterial(sample);
                  shapeB.setMaterial(sample);
                  return true;
              }
          }
      }

      return false;
  }

  boolean isCollisionRecursive(ArrayList<Triangle3D> objectA, ArrayList<Triangle3D> objectB, float minX, float maxX, 
		  float minY, float maxY, float minZ, float maxZ, int iA, int iB, int rec_depth){
	  //performs octree algorithm until a small set of triangles can be operated upon by brute force, i.e. greatly reducing the search space
	  //first define our new midpoints
	  rec_depth += 1;
	  float midX = (minX + maxX) / 2;
	  float midY = (minY + maxY) / 2;
	  float midZ = (minZ + maxZ) / 2;
//	  System.out.println("minX: " + minX + " maxX: " + maxX);
//	  System.out.println("minY: " + minY + " maxY: " + maxY);
//	  System.out.println("minZ: " + minZ + " maxZ: " + maxZ);

	  //build the new arrays as we need them to save on object references
	  //using ArrayList so we skip all the bookkeeping involved in using regular arrays for this
	  // it may improve performance to drop them later
	  //0 means min to mid, 1 means mid to max
	  //e.g. array 0_0_0 is (midX-minX,midY-minY,midZ-minZ)
	  //whereas array 1_1_1 is (maxX-midX,maxY-midY,maxZ-midZ)
	  //define one array for A, one array for B
//	  System.out.println("In isCollisionRecursive");
//	  System.out.println("Our box is: [" + minX + "," + maxX + "," + minY + "," + maxY + "," + minZ + "," + maxZ + "]");
//	  System.out.println("midX = " + midX + " ; midY = " + midY + " ; midZ = " + midZ);

	  for(int i = 0; i < 8; i++){
		  //do this 8 times
//		  System.out.println("In inner loop, i = " + i);
		  //now use the bit representation of 8 for this
		  boolean startX = ((i & 0x0000000001) == 1);
		  boolean startY = ((i & 0x00000002) == 2);
		  boolean startZ = ((i & 0x00000004) == 4);
//		  System.out.println("This is box " + startX + "," + startY+"," + startZ);
		  float[] xCoords,yCoords,zCoords;
		  xCoords = startX ? new float[]{midX,maxX} : new float[]{minX,midX};
		  yCoords = startY ? new float[]{midY,maxY} : new float[]{minY,midY};
		  zCoords = startZ ? new float[]{midZ,maxZ} : new float[]{minZ,midZ};
//		  System.out.println("Arrays are ( i = " + i + " ): \n");
		  ArrayList<Triangle3D> array_A = getTrianglesInBox(objectA,xCoords[0],xCoords[1],yCoords[0],yCoords[1],zCoords[0],zCoords[1]);
		  ArrayList<Triangle3D> array_B = getTrianglesInBox(objectB,xCoords[0],xCoords[1],yCoords[0],yCoords[1],zCoords[0],zCoords[1]);
		  int product = array_A.size() * array_B.size();
		  boolean found_collision = false; 
//		  System.out.println("number of triangles from A  in box with dimensions [" + xCoords[0] + "," + xCoords[1] + "," + yCoords[0] + "," + yCoords[1] + "," + zCoords[0] + "," + zCoords[1] + "] is: " + array_A.size());
//		  System.out.println("number of triangles from B  in box with dimensions [" + xCoords[0] + "," + xCoords[1] + "," + yCoords[0] + "," + yCoords[1] + "," + zCoords[0] + "," + zCoords[1] + "] is: " + array_B.size());
		  if(product != 0){
			  if(product <= 10000 || rec_depth >= 5){
				  found_collision = isCollision(array_A,array_B,iA,iB);
			  }else{
				  found_collision = isCollisionRecursive(array_A,array_B,xCoords[0],xCoords[1],yCoords[0],yCoords[1],zCoords[0],zCoords[1],iA,iB,rec_depth);
			  }
			  if(found_collision){
				  return true; 
				  //if it finds a collision, exit out of this whole thing since we only have to check if any part collides with any other part
				  //otherwise keep going until we check all the boxen, because some of the others may collide
			  }
		  }
	  }
	  //if we leave the loop, we never found a collision in our smaller boxen
	  return false;
  }

void crossSection(  ) {
      path3D.getChildren().clear();
      double zHeight = (
              rootMeshRotates.getBoundsInLocal().getMaxZ()-
              rootMeshRotates.getBoundsInLocal().getMinZ()  );
      int nSections = 10;
      for ( int iSection = 0; iSection < nSections; iSection++ ) {
          crossSection(rootMeshRotates.getBoundsInLocal().getMinZ() + zHeight*((iSection+0.5)/nSections));
      }
  }

  void crossSection( double zLevel ) {
      for (int iMesh = 0; iMesh < rootMeshRotates.getChildren().size(); iMesh++) {
          MeshView     shape = (MeshView)rootMeshRotates.getChildren().get(iMesh);
          TriangleMesh  mesh = (TriangleMesh)shape.getMesh();
          for (int iFace = 0; iFace < mesh.getFaces().size()/mesh.getFaceElementSize(); iFace++ ) {
              //System.out.println("iFace:" + iFace);
              int iVertex0 = mesh.getFaces().get(iFace*6+0); // i*6+
              int iVertex1 = mesh.getFaces().get(iFace*6+2);
              int iVertex2 = mesh.getFaces().get(iFace*6+4);

              Point3D a0 = shape.localToParent( mesh.getPoints().get(iVertex0*3+0), 
                                                mesh.getPoints().get(iVertex0*3+1), 
                                                mesh.getPoints().get(iVertex0*3+2) );
              Point3D a1 = shape.localToParent( mesh.getPoints().get(iVertex1*3+0), 
                                                mesh.getPoints().get(iVertex1*3+1), 
                                                mesh.getPoints().get(iVertex1*3+2) );
              Point3D a2 = shape.localToParent( mesh.getPoints().get(iVertex2*3+0), 
                                                mesh.getPoints().get(iVertex2*3+1), 
                                                mesh.getPoints().get(iVertex2*3+2) );
              Triangle3D a = new Triangle3D( a0, a1, a2 );

              Pair3D pair = a.intersectPlaneZ( (float) zLevel );

              if (pair != null) {
                  Point3D point1 = rootMeshAll.localToParent(rootMeshRotates.localToParent(pair.a.x,pair.a.y,zLevel));
                  Point3D point2 = rootMeshAll.localToParent(rootMeshRotates.localToParent(pair.b.x,pair.b.y,zLevel));
    
                  List<org.fxyz.geometry.Point3D> pointPath = new ArrayList<org.fxyz.geometry.Point3D>();
                  pointPath.add(new org.fxyz.geometry.Point3D((float)point1.getX(),(float)point1.getY(),(float)point1.getZ()));
                  pointPath.add(new org.fxyz.geometry.Point3D((float)point2.getX(),(float)point2.getY(),(float)point2.getZ()));
                  PolyLine3D polyLine = new PolyLine3D(pointPath,5,Color.WHITE);
                  path3D.getChildren().add(polyLine);
              }

              //Point3D a00 = shape.localToParent( mesh.getPoints().get(0), 
              //          mesh.getPoints().get(1), 
              //          mesh.getPoints().get(2) );
              //System.out.println("Shape A at 0 "+a00.getX()+" "+a00.getY()+" "+a00.getZ());
          }
      }
  }

}
