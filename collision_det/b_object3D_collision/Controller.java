package b_object3D_collision;

import javafx.event.ActionEvent;
import javafx.geometry.Point3D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class Controller {
    
    private ViewSTL view;

    private double targetX = 0;
    private double targetY = 0;
    private Point3D location0 = new Point3D(0,0,0);

    private boolean isCheckCollisions = false;
    private boolean isCrossSection    = false;
    private MeshView lastVisitedMeshView = null;
    private boolean collisionSlow = false;
    
    void setView(ViewSTL view) {
        this.view = view;
    }

    public void notifyMousePressed(MouseEvent e, Group target)
    {
        if(target==null)
        {
            System.out.println("Controller: pressed in null");
            if ( e.getClickCount() > 1 )
            { // Multiple click
                // Do nothing
            }
        }
        else
        {
            targetX = e.getSceneX();
            targetY = e.getSceneY();
            location0 = rotationalLocation(targetX,targetY,
                    target.getScene().getWidth(),target.getScene().getHeight());

            if (e.getClickCount() > 1)
            { // Multiple click
                // Do nothing
            }
            else
            { // Single click
                System.out.println("Controller: pressed in node " + targetX + " " + targetY);
                // Old node position system torn out
                target.setCursor(Cursor.MOVE);
            }
        }
    }
    public void notifyMouseReleased(MouseEvent e, Group target)
    {
        if(target==null)
        {
            System.out.println("Controller: released in null");
        }
        else
        {
            System.out.println("Controller: released in node");
            target.setCursor(Cursor.HAND);
        }
    }
    public void notifyMouseDragged(MouseEvent e, Group target)
    {
        if(target==null)
        {
            System.out.println("Controller: dragged in null");
        }
        else
        {
            Point3D location1 = rotationalLocation(e.getSceneX(),e.getSceneY(),
                    target.getScene().getWidth(),target.getScene().getHeight());
            double         angle = location1.angle(       location0);
            Point3D rotationAxis = location1.crossProduct(location0);
            try {
                rotationAxis = target.getLocalToParentTransform().inverseTransform(rotationAxis);
            } catch (NonInvertibleTransformException e1) {
                // Auto-generated catch block
                e1.printStackTrace();
            }
            //System.out.println("Controller: dragged in node. angle:" + angle
            //        + " axsX:" + rotationAxis.getX() + " axsY:" + rotationAxis.getY() + " axsZ:" + rotationAxis.getZ()  
            //        + " scnX:" +       e.getSceneX() + " scnY:" +       e.getSceneY() 
            //        + " getX:" +            e.getX() + " getY:" +            e.getY());
            //System.out.println(
            //        " scnX:" +       e.getSceneX() + " scnY:" +       e.getSceneY() 
            //        + " wX:" + target.getScene().getWidth() + " wY:" + target.getScene().getHeight());
            //System.out.println(location1.toString());//rotationAxis.toString());//
            System.out.println("rotationAxis:"+rotationAxis.toString()+" angle: "+angle);//locationB.toString());//
            view.rotate( -angle, rotationAxis, isCrossSection);
            if ( isCrossSection ) view.crossSection();
            // Set the vertex position to the cursor position
            targetX = e.getSceneX();
            targetY = e.getSceneY();
            location0 = location1;
        }
    }
    
    private Point3D rotationalLocation( double x, double y, double wX, double wY) {
        double sceneX = x - wX/2;
        double sceneY = y - wY/2;
        sceneX = sceneX/Math.min(wX,wY)*2;
        sceneY = sceneY/Math.min(wX,wY)*2;
        sceneX = Math.min(1,Math.max(-1,sceneX));
        sceneY = Math.min(1,Math.max(-1,sceneY));
        double sceneZ = 1-sceneX*sceneX-sceneY*sceneY;
        sceneZ = (sceneZ>0)? Math.sqrt(sceneZ) : 0;
        return new Point3D(sceneX,sceneY,-sceneZ);
    }

    public void notifyMouseEntered(MouseEvent e, Group target)
    {
        if(target==null)
        {
            System.out.println("Controller: entered null");
        }
        else
        {
            System.out.println("Controller: entered node");
            target.setCursor(Cursor.HAND);
        }
    }

    public void notifyMouseExited(MouseEvent e, Group rootAll) {
    }

    public void notifyMouseEnteredMesh(MouseEvent e, MeshView meshView) {
        System.out.println("Controller: entered MeshView node");
        lastVisitedMeshView = meshView;        
    }

    public void notifyWireFrameButton(ActionEvent e) {
        ToggleButton source = (ToggleButton) e.getSource();
            if (source.isSelected()) {
                view.setRender( true  ); // set to isWireframe
            } else {
                view.setRender( false ); // set against isWireframe
            }
    }

    public void notifyCrossSectnButton(ActionEvent e) {
        ToggleButton source = (ToggleButton) e.getSource();
            if (source.isSelected()) {
                view.setColorsDarker(); 
                isCrossSection = true;
                view.crossSection();
            } else {
                view.setColorsNormal(); 
                isCrossSection = false;
            }
    }

    public void notifyRenderBackButton(ActionEvent e) {
        ToggleButton source = (ToggleButton) e.getSource();
        if (source.isSelected()) {
            System.out.println("Controller: BackFaceOn mode");
            view.setBackFace( true  ); // set to isDisplayBack
        } else {
            System.out.println("Controller: BackFaceOff mode");
            view.setBackFace( false ); // set against isDisplayBack
        }
    }

    public void notifyCheckCllsnButton(ActionEvent e) {
        ToggleButton source = (ToggleButton) e.getSource();
            if (source.isSelected()) {
                System.out.println("Controller: CheckCollisionsOn mode");
                isCheckCollisions = true ;
                collisionSlow = false;
                view.isCollision(false); 
            } else {
                System.out.println("Controller: CheckCollisionsOff mode");
                view.setColorsNormal();
                isCheckCollisions = false;
            }
    }
    
    public void notifySlowCollisionButton(ActionEvent e){
        ToggleButton source = (ToggleButton) e.getSource();
        if (source.isSelected()) {
            System.out.println("Controller: CheckCollisionsOn mode");
            isCheckCollisions = true ;
            collisionSlow = true;
            view.isCollision(true); 
        } else {
            System.out.println("Controller: CheckCollisionsOff mode");
            view.setColorsNormal();
            isCheckCollisions = false;
        }
    }

    public void notifyAddSTLFileButton(ActionEvent e) {
        view.loadMeshView();        
    }

    public void notifyRemoveFileButton(ActionEvent e) {
        if (lastVisitedMeshView != null ) {
            view.removeMeshView( lastVisitedMeshView );
            lastVisitedMeshView  = null;
        }
    }

    public void notifyScrollMove(ScrollEvent e) {
        System.out.println("ScrollEvent "+e.getDeltaY());
        view.translateAllMeshes( e.getDeltaY() * .001 );
    }

    public void notifyKeyPressed(KeyEvent e) {
        //System.out.println("Controller: key " + e.getCode() + "pressed");
        if (lastVisitedMeshView != null) {
            if         ( e.getCode()== KeyCode.LEFT ) {
                // Note, this may only be particular to my laptop.
                // However, after the first time LEFT key is pressed, 
                //   all of (LEFT, RIGHT, UP, DOWN) no longer work.
                // Consequently, for LEFT and RIGHT, digits 5 and 6 are 
                //   used
                // Digits(0..9) were mostly picked in order to avoid 
                //   Dvorak vs QWERTY issues
                ;
            } else if  ( e.getCode()== KeyCode.RIGHT) {
                ;
            } else if  ( e.getCode()== KeyCode.DOWN ) {
                ;
            } else if  ( e.getCode()== KeyCode.UP   ) {
                ;
            } else if  ( e.getCode()== KeyCode.DIGIT1) {
                lastVisitedMeshView.getTransforms().add( new Rotate(10,Rotate.X_AXIS) );
                if (isCheckCollisions)view.isCollision(collisionSlow); // This checks all objects, not only the moved one???
            } else if  ( e.getCode()== KeyCode.DIGIT2) {
                lastVisitedMeshView.getTransforms().add( new Rotate(10,Rotate.Y_AXIS) );
                if (isCheckCollisions) view.isCollision(collisionSlow); // This checks all objects, not only the moved one???
            } else if  ( e.getCode()== KeyCode.DIGIT3) {
                lastVisitedMeshView.getTransforms().add( new Rotate(10,Rotate.Z_AXIS) );
                if (isCheckCollisions) view.isCollision(collisionSlow); // This checks all objects, not only the moved one???
            } else if  ( e.getCode()== KeyCode.DIGIT4) {
                ;
            } else if  ( e.getCode()== KeyCode.DIGIT5  ) {
                //System.out.println("   Controller: key DIGIT5 pressed");
                lastVisitedMeshView.getTransforms().add( new Translate( -1, 0, 0) );
                if (isCheckCollisions) view.isCollision(collisionSlow); // This checks all objects, not only the moved one???
            } else if  ( e.getCode()== KeyCode.DIGIT6) {
                //System.out.println("   Controller: key DIGIT6 pressed");
                lastVisitedMeshView.getTransforms().add( new Translate(  1, 0, 0) );
                if (isCheckCollisions) view.isCollision(collisionSlow); // This checks all objects, not only the moved one???
            } else if  ( e.getCode()== KeyCode.DIGIT7) {
                lastVisitedMeshView.getTransforms().add( new Translate(  0, 0,-1) );
                if (isCheckCollisions) view.isCollision(collisionSlow); // This checks all objects, not only the moved one???
            } else if  ( e.getCode()== KeyCode.DIGIT8) {
                lastVisitedMeshView.getTransforms().add( new Translate(  0, 0, 1) );
                if (isCheckCollisions) view.isCollision(collisionSlow); // This checks all objects, not only the moved one???
            } else if  ( e.getCode()== KeyCode.DIGIT9) {
                lastVisitedMeshView.getTransforms().add( new Translate(  0,-1, 0) );
                if (isCheckCollisions) view.isCollision(collisionSlow); // This checks all objects, not only the moved one???
            } else if  ( e.getCode()== KeyCode.DIGIT0) {
                lastVisitedMeshView.getTransforms().add( new Translate(  0, 1, 0) );
                if (isCheckCollisions) view.isCollision(collisionSlow); // This checks all objects, not only the moved one???
            } else if  ( e.getCode()== KeyCode.O || e.getCode()== KeyCode.R  ) {// R for Dvorak
                view.translateAllMeshes(  0.1);
            } else if  ( e.getCode()== KeyCode.I || e.getCode()== KeyCode.C  ) {// C for Dvorak
                view.translateAllMeshes( -0.1);
            } else if  ( e.getCode()== KeyCode.M   ) {
                view.isCollision(false);
            } else if  ( e.getCode()== KeyCode.B   ) {
            	view.isCollision(true);
            } else if  ( e.getCode()== KeyCode.UP   ) {
                ;
            }
            if ( isCrossSection ) view.crossSection();
        } else { // lastVisitedMeshView == null
            if  ( e.getCode()== KeyCode.A  ) {// A is A in QWERTY or Dvorak
            	 MeshView  leftMesh = view.loadMeshView(); // Load up LemurHandLeft.stl        
                 MeshView rightMesh = view.loadMeshView(); // Load up LemurHandRight.stl
                 rightMesh.getTransforms().add( new Translate(0,-15,0));// new Translate(0,-15,0));// 
                 rightMesh.getTransforms().add( new Translate(-43,0,0));// new Translate(-45,0,0));// 
                 rightMesh.getTransforms().add( new Rotate(10,Rotate.X_AXIS) );

                 long sumOfDurations = 0;
                 {
                     // Start the timing
                     long startTime = System.nanoTime();
                     view.isCollision(false);
                     // Finish the timing
                     long endTime  = System.nanoTime();
                     long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
                     long durSec   =  duration/1000000000L;
                     long durMilli = (duration-durSec*1000000000L)/1000000;
                     System.out.println("This comparison took "+duration+" nanoseconds. ("+durSec+"."+durMilli+" seconds)");
                     sumOfDurations += duration;
                 }

                 view.rotate(  72, new Point3D(1,1,0), isCrossSection);
                 {
                     // Start the timing
                     long startTime = System.nanoTime();
                     view.isCollision(false);
                     // Finish the timing
                     long endTime  = System.nanoTime();
                     long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
                     long durSec   =  duration/1000000000L;
                     long durMilli = (duration-durSec*1000000000L)/1000000;
                     System.out.println("This comparison took "+duration+" nanoseconds. ("+durSec+"."+durMilli+" seconds)");
                     sumOfDurations += duration;
                 }

                 view.rotate(  43, new Point3D(0,1,0), isCrossSection);
                 {
                     // Start the timing
                     long startTime = System.nanoTime();
                     view.isCollision(false);
                     // Finish the timing
                     long endTime  = System.nanoTime();
                     long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
                     long durSec   =  duration/1000000000L;
                     long durMilli = (duration-durSec*1000000000L)/1000000;
                     System.out.println("This comparison took "+duration+" nanoseconds. ("+durSec+"."+durMilli+" seconds)");
                     sumOfDurations += duration;
                 }

                 view.rotate(  47, new Point3D(0,1,1), isCrossSection);
                 {
                     // Start the timing
                     long startTime = System.nanoTime();
                     view.isCollision(false);
                     // Finish the timing
                     long endTime  = System.nanoTime();
                     long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
                     long durSec   =  duration/1000000000L;
                     long durMilli = (duration-durSec*1000000000L)/1000000;
                     System.out.println("This comparison took "+duration+" nanoseconds. ("+durSec+"."+durMilli+" seconds)");
                     sumOfDurations += duration;
                 }

                 leftMesh.getTransforms().add( new Translate(2,0,0));// new Translate(-45,0,0));// 
                 {
                     // Start the timing
                     long startTime = System.nanoTime();
                     view.isCollision(false);
                     // Finish the timing
                     long endTime  = System.nanoTime();
                     long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
                     long durSec   =  duration/1000000000L;
                     long durMilli = (duration-durSec*1000000000L)/1000000;
                     System.out.println("This comparison took "+duration+" nanoseconds. ("+durSec+"."+durMilli+" seconds)");
                     sumOfDurations += duration;
                 }

                 System.out.println("All comparisons took "+sumOfDurations+" nanoseconds. ("+sumOfDurations/1000000000L+" seconds)");
//                MeshView  leftMesh = view.loadMeshView(); // Load up LemurHandLeft.stl        
//                MeshView rightMesh = view.loadMeshView(); // Load up LemurHandRight.stl
//                rightMesh.getTransforms().add( new Translate(0,-15,0));
//                rightMesh.getTransforms().add( new Translate(-43,0,0));
//                rightMesh.getTransforms().add( new Rotate(10,Rotate.X_AXIS) );
//
//                // Start the timing
//                long startTime = System.nanoTime();
//                view.isCollision(false);
//                // Finish the timing
//                long endTime  = System.nanoTime();
//                long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
//                long durSec   =  duration/1000000000L;
//                long durMilli = (duration-durSec*1000000000L)/1000000;
//                System.out.println("This comparison took "+duration+" nanoseconds. ("+durSec+"."+durMilli+" seconds)");
            }
        }       
    }
}
