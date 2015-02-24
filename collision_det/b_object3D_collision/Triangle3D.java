package b_object3D_collision;

import java.io.ByteArrayOutputStream;

import javafx.geometry.Point3D;



import scpsolver.problems.LinearProgram;
import scpsolver.constraints.LinearEqualsConstraint;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.lpsolver.*; //there's only two classes in here anyway

// This triangle/matrix manipulation package is incomplete 
//  and in some places lacking approaches to handle numerical
public class Triangle3D {
    float [] a = new float[3];
    float [] b = new float[3];
    float [] c = new float[3];

    Triangle3D() { }
    
    Triangle3D(
            float ax, float ay, float az,
            float bx, float by, float bz,
            float cx, float cy, float cz
            ) 
    {
        this.a[0] = ax;  this.a[1] = ay;  this.a[2] = az;
        this.b[0] = bx;  this.b[1] = by;  this.b[2] = bz;
        this.c[0] = cx;  this.c[1] = cy;  this.c[2] = cz;
    }
    
    Triangle3D(
            Point3D a, // These are row vectors
            Point3D b,
            Point3D c
            ) 
    {
        this.a[0] = (float)a.getX();  this.a[1] = (float)a.getY();  this.a[2] = (float)a.getZ();
        this.b[0] = (float)b.getX();  this.b[1] = (float)b.getY();  this.b[2] = (float)b.getZ();
        this.c[0] = (float)c.getX();  this.c[1] = (float)c.getY();  this.c[2] = (float)c.getZ();
    }

    Triangle3D(
            Vector3D a, // These are row vectors
            Vector3D b,
            Vector3D c
            ) 
    {
        this.a[0] = a.x;  this.a[1] = a.y;  this.a[2] = a.z;
        this.b[0] = b.x;  this.b[1] = b.y;  this.b[2] = b.z;
        this.c[0] = c.x;  this.c[1] = c.y;  this.c[2] = c.z;
    }

    public class Vector3D{  // This a row vector by the convention of this file
                            // At times is used as a column vector. I.e., (x,y,z) is not T(a,b,c)
        float x; float y; float z;
        static final float EPSILON = 1e-6f; // Guessing. What should this be?
        static final float EPSILON_B = EPSILON*100; // Guessing. What should this be?
        Vector3D() { }
        Vector3D(float a, float b, float c) { this.x=  a; this.y=  b; this.z=  c; }
        Vector3D(Vector3D v)                { this.x=v.x; this.y=v.y; this.z=v.z; }
        Vector3D minus(         Vector3D v2) { return new Vector3D(x-  v2.x,y-  v2.y,z-  v2.z); }
        Vector3D  plus(         Vector3D v2) { return new Vector3D(x+  v2.x,y+  v2.y,z+  v2.z); }
        Vector3D  plus(float m, Vector3D v2) { return new Vector3D(x+m*v2.x,y+m*v2.y,z+m*v2.z); }
        Vector3D times( Triangle3D m ) { return new Vector3D( // this is a row vector and we are doing this*m
                this.x*m.a[0] + this.y*m.b[0] + this.z*m.c[0],
                this.x*m.a[1] + this.y*m.b[1] + this.z*m.c[1],
                this.x*m.a[2] + this.y*m.b[2] + this.z*m.c[2]  ); }
        Vector3D solve( Triangle3D m ) { // This is the row vector version
            Vector3D r = new Vector3D(
                    m.b[1]*m.c[2]-m.c[1]*m.b[2],
                    m.c[1]*m.a[2]-m.a[1]*m.c[2],
                    m.a[1]*m.b[2]-m.b[1]*m.a[2] );
            float det = m.a[0]*r.x + m.b[0]*r.y + m.c[0]*r.z;
            if (det != 0.f) {
                r.x = (r.x * x - (m.b[0]*m.c[2]-m.c[0]*m.b[2]) * y + (m.b[0]*m.c[1]-m.c[0]*m.b[1]) * z) / det;
                r.y = (r.y * x - (m.c[0]*m.a[2]-m.a[0]*m.c[2]) * y + (m.c[0]*m.a[1]-m.a[0]*m.c[1]) * z) / det;
                r.z = (r.z * x - (m.a[0]*m.b[2]-m.b[0]*m.a[2]) * y + (m.a[0]*m.b[1]-m.b[0]*m.a[1]) * z) / det;
            } else { // The matrix is not invertible
                r.x = Float.MAX_VALUE;
            }
            return r;
            //float a[0],a[1],a[2];
            //float b[0],b[1],b[2];
            //float c[0],c[1],c[2];
        }
        Vector3D solveT( Triangle3D m ) { // Column vector version
            assert(false); // We need a check for whether the matrix is ill conditioned as in solve();
            Vector3D r = new Vector3D(
                    m.b[1]*m.c[2]-m.b[2]*m.c[1],
                    m.b[2]*m.c[0]-m.b[0]*m.c[2],
                    m.b[0]*m.c[1]-m.b[1]*m.c[0] );
            float det = m.a[0]*r.x + m.a[1]*r.y + m.a[2]*r.z;
            if (det != 0.f) {
                r.x = (r.x * x - (m.a[1]*m.c[2]-m.a[2]*m.c[1]) * y + (m.a[1]*m.b[2]-m.a[2]*m.b[1]) * z) / det;
                r.y = (r.y * x - (m.a[2]*m.c[0]-m.a[0]*m.c[2]) * y + (m.a[2]*m.b[0]-m.a[0]*m.b[2]) * z) / det;
                r.z = (r.z * x - (m.a[0]*m.c[1]-m.a[1]*m.c[0]) * y + (m.a[0]*m.b[1]-m.a[1]*m.b[0]) * z) / det;
                
                // Check the result
                Vector3D chck = times(m).minus(this);
                if ( ! (-EPSILON_B < chck.x && chck.x < EPSILON_B &&
                        -EPSILON_B < chck.y && chck.y < EPSILON_B &&
                        -EPSILON_B < chck.z && chck.z < EPSILON_B    ) ) {
                    r.x = Float.MAX_VALUE/2; // The matrix is ill conditioned enough to cause numerical issues
                }
            } else { // The matrix is not invertible
                r.x = Float.MAX_VALUE;
            }
            return r;
        }
        String print() { return "V3D: "+x+", "+y+", "+z+"."; }
        protected boolean isCollision(Vector3D v, Vector3D p, Vector3D vA, Vector3D vB) {
            // true if point(this) + m * vector(v) == point(p) + a * vector(vA) + b * vector(vB)
            //         for some 0 <= m,a,b <= 1 and a+b <= 1 
            // else false
            
            // Solve for pd: (this - p) == pd * (v,vA,vB). Consequently, m = -pd.x
            Vector3D pd = minus(p).solve(new Triangle3D(v,vA,vB));
            
            if (    pd.x <  EPSILON && (-1f-EPSILON) < pd.x &&
                    pd.y > -EPSILON && pd.z > -EPSILON &&
                    pd.y+pd.z < (1f+EPSILON) ) 
                return true ;
            else // TODO else if pd.x==Float.MAX_VALUE
                return false;
        }
    }

    public class Pair3D { // Simply a pair of triplets
        Vector3D a;
        Vector3D b;
        Pair3D () {
            a = new Vector3D();
            b = new Vector3D();
        }
    }

    boolean isCollisionLP(Triangle3D t1, ByteArrayOutputStream baos){
    	//This uses LPSOLVE to hopefully do things faster than regular isCollision
    	//for triangles with vertices ABC DEF, we only need points a, d, and the segments AB, AC, DE, DF.
    	//so first let's set up all our variables
    	///we need a lot of them because each equation will be solved in one coordinate. 
    	//this is because a linear program looks like this:
    	//Min(5x + 10y)
    	//but we have x as a vector, not as a float, and this program only supports floats
    	//therefore we need to set up a ton of variables, and 8 total equations
    	//our equation is:
    	//0 <= (A-D) + B_0 V(AB) + B_1 V(AC) - B_2 V(DE) - B_3 V(DF)
    	//in fact, it is exactly = 0, so we can do >= and <=. This means we can rearrange:
    	//-(A-D) == B_0 V(AB) + B_1 V(AC) - B_2 V(DE) - B_3 V(DF)
    	//cast to doubles because LPSolver likes doubles
    	//we pass baos as an agrument purely so we can check whether the soln is unbounded or not
    	//and so we don't open a ton of outputstreams, slowing down our algorithm
    	double[] dummy = new double[]{-1.0*(a[0] - t1.a[0]), -1.0*(a[1] - t1.a[1]), -1.0*(a[2] - t1.a[2])}; //-1 so we can use it in the constraints
    	double[] Vab = new double[]{b[0]-a[0],b[1]-a[1],b[2]-a[2]};
    	double[] Vac = new double[]{c[0]-a[0],c[1]-a[1],c[2]-a[2]};
    	//make sure DE and DF are negative so we don't have to write extra below
    	double[] Vde = new double[]{-1.0*(t1.b[0]-t1.a[0]),-1.0*(t1.b[1]-t1.a[1]),-1.0*(t1.b[2]-t1.a[2])};
    	double[] Vdf = new double[]{-1.0*(t1.c[0]-t1.a[0]),-1.0*(t1.c[1]-t1.a[1]),-1.0*(t1.c[2]-t1.a[2])};
    	LinearProgram lp = new LinearProgram(new double[]{1.0,1.0,1.0,1.0}); 
		lp.addConstraint(new LinearEqualsConstraint(new double[]{Vab[0],Vac[0],Vde[0],Vdf[0]}, dummy[0], "c1")); 
		lp.addConstraint(new LinearEqualsConstraint(new double[]{Vab[1],Vac[1],Vde[1],Vdf[1]}, dummy[1], "c2")); 
		lp.addConstraint(new LinearEqualsConstraint(new double[]{Vab[2],Vac[2],Vde[2],Vdf[2]}, dummy[2], "c3")); 
		lp.addConstraint(new LinearSmallerThanEqualsConstraint(new double[]{1.0,1.0,0.0,0.0}, 1.0, "c4")); 
		lp.addConstraint(new LinearSmallerThanEqualsConstraint(new double[]{0.0,0.0,1.0,1.0}, 1.0, "c5")); 
		lp.addConstraint(new LinearBiggerThanEqualsConstraint(new double[]{1.0,0.0,0.0,0.0}, 0.0, "c6")); 
		lp.addConstraint(new LinearBiggerThanEqualsConstraint(new double[]{0.0,1.0,0.0,0.0}, 0.0, "c7")); 
		lp.addConstraint(new LinearBiggerThanEqualsConstraint(new double[]{0.0,0.0,1.0,0.0}, 0.0, "c8")); 
		lp.addConstraint(new LinearBiggerThanEqualsConstraint(new double[]{0.0,0.0,0.0,1.0}, 0.0, "c9")); 
		lp.setMinProblem(true); 
		LinearProgramSolver solver  = SolverFactory.newDefault(); 
		double[] sol = solver.solve(lp);
		for(double x : sol){
			if (x == 0.0) return false;
		}
		//String soln_issue = baos.toString();
		//if any of the solutions are 0, we quit
		//if(soln_issue.indexOf("infeasible") > -1) return false;
    	return true;
    }
    
    boolean isCollision(Triangle3D t) { // Check for collision between two triangles
        Vector3D p0  = new Vector3D(  a[0]       ,  a[1]       ,  a[2]       );
        Vector3D v10 = new Vector3D(  b[0]-  a[0],  b[1]-  a[1],  b[2]-  a[2]);
        Vector3D v20 = new Vector3D(  c[0]-  a[0],  c[1]-  a[1],  c[2]-  a[2]);

        Vector3D p3  = new Vector3D(t.a[0]       ,t.a[1]       ,t.a[2]       );
        Vector3D v43 = new Vector3D(t.b[0]-t.a[0],t.b[1]-t.a[1],t.b[2]-t.a[2]);
        Vector3D v53 = new Vector3D(t.c[0]-t.a[0],t.c[1]-t.a[1],t.c[2]-t.a[2]);

        if ( p0.isCollision(v10, p3, v43, v53) ) return true;
        if ( p0.isCollision(v20, p3, v43, v53) ) return true;
        if ( p3.isCollision(v43, p0, v10, v20) ) return true;
        if ( p3.isCollision(v53, p0, v10, v20) ) return true;

        // Need to check one more edge
        Vector3D p1  = new Vector3D(  b[0]       ,  b[1]       ,  b[2]       );
        Vector3D v21 = new Vector3D(  c[0]-  b[0],  c[1]-  b[1],  c[2]-  b[2]);

        if ( p1.isCollision(v21, p3, v43, v53) ) return true;

        return false; 
    }

    boolean isCollisionBoundingBoxExhaustive( 
            float [] minP, float [] maxP   ) { // 3D coordinates 
                           // of the min and max of the bounding box

        // This is the exhaustive check for whether a triangle 
        //   intersects any faces of a bounding box.
        
        // The code is meant to be more demonstrative than optimal
        //   Thus, there is much more that one can do to speed this up
        
        // There are three edges of the triangle that we will check
        //   for intersections with the six bounding box sides.
        //   Thus, there are eighteen checks
        
        float[] vBA = {  b[0]-  a[0],  b[1]-  a[1],  b[2]-  a[2]};
        if (isCollisionSegmentToBoxFace(a,vBA,0,minP[0],minP[1],minP[2],maxP[1],maxP[2])) return true;
        if (isCollisionSegmentToBoxFace(a,vBA,0,maxP[0],minP[1],minP[2],maxP[1],maxP[2])) return true;
        if (isCollisionSegmentToBoxFace(a,vBA,1,minP[1],minP[2],minP[0],maxP[2],maxP[0])) return true;
        if (isCollisionSegmentToBoxFace(a,vBA,1,maxP[1],minP[2],minP[0],maxP[2],maxP[0])) return true;
        if (isCollisionSegmentToBoxFace(a,vBA,2,minP[2],minP[0],minP[1],maxP[0],maxP[1])) return true;
        if (isCollisionSegmentToBoxFace(a,vBA,2,maxP[2],minP[0],minP[1],maxP[0],maxP[1])) return true;
        float[] vCB = {  c[0]-  b[0],  c[1]-  b[1],  c[2]-  b[2]};
        if (isCollisionSegmentToBoxFace(b,vCB,0,minP[0],minP[1],minP[2],maxP[1],maxP[2])) return true;
        if (isCollisionSegmentToBoxFace(b,vCB,0,maxP[0],minP[1],minP[2],maxP[1],maxP[2])) return true;
        if (isCollisionSegmentToBoxFace(b,vCB,1,minP[1],minP[2],minP[0],maxP[2],maxP[0])) return true;
        if (isCollisionSegmentToBoxFace(b,vCB,1,maxP[1],minP[2],minP[0],maxP[2],maxP[0])) return true;
        if (isCollisionSegmentToBoxFace(b,vCB,2,minP[2],minP[0],minP[1],maxP[0],maxP[1])) return true;
        if (isCollisionSegmentToBoxFace(b,vCB,2,maxP[2],minP[0],minP[1],maxP[0],maxP[1])) return true;
        float[] vAC = {  a[0]-  c[0],  a[1]-  c[1],  a[2]-  c[2]};
        if (isCollisionSegmentToBoxFace(c,vAC,0,minP[0],minP[1],minP[2],maxP[1],maxP[2])) return true;
        if (isCollisionSegmentToBoxFace(c,vAC,0,maxP[0],minP[1],minP[2],maxP[1],maxP[2])) return true;
        if (isCollisionSegmentToBoxFace(c,vAC,1,minP[1],minP[2],minP[0],maxP[2],maxP[0])) return true;
        if (isCollisionSegmentToBoxFace(c,vAC,1,maxP[1],minP[2],minP[0],maxP[2],maxP[0])) return true;
        if (isCollisionSegmentToBoxFace(c,vAC,2,minP[2],minP[0],minP[1],maxP[0],maxP[1])) return true;
        if (isCollisionSegmentToBoxFace(c,vAC,2,maxP[2],minP[0],minP[1],maxP[0],maxP[1])) return true;

        return false;
    }
    
    static boolean isCollisionSegmentToBoxFace( float [] point, float [] vector, int dimension0, 
            float level, float min1, float min2, float max1, float max2) {
        // dimension0 - 0 for x, 1 for y, 2 for z
        // level - location of the bounding box in the dimension above
        // min1, min2, max1, max2 - other bounds of the bounding box shape in question

        if (vector[dimension0]<=0) return false; // There is no thickness in this dimension

        // This is the proportion of distance to appropriate face plane from point.
        float alpha = (level - point[dimension0]) / vector[dimension0];
        if (alpha < 0 || 1 < alpha) return false; // The bounding box face is too far
        
        int dimension1 = (dimension0+1)%3; // enumerate the other dimensions
        float value1 = point[dimension1]+alpha*vector[dimension1];
        if (value1 < min1 || max1 < value1) return false;
        
        int dimension2 = (dimension0+2)%3; // enumerate the other dimensions
        float value2 = point[dimension2]+alpha*vector[dimension2];
        if (value2 < min2 || max2 < value2) return false;

        return true;
    }

    Pair3D intersectPlaneZ( float zLevel ) {
        // Point A vs. B1+B2 on different sides where A,B1,B2 in cross-section CW order
        float [] pA = null, pB1 = null, pB2 = null; // 

        if (     a[2]>zLevel && b[2]> zLevel && c[2]> zLevel) { return null;        }
        else if (a[2]<zLevel && b[2]< zLevel && c[2]< zLevel) { return null;        }
        else if (a[2]>zLevel && b[2]<=zLevel && c[2]<=zLevel) { pA=a; pB1=b; pB2=c; }
        else if (b[2]>zLevel && c[2]<=zLevel && a[2]<=zLevel) { pA=b; pB1=c; pB2=a; }
        else if (c[2]>zLevel && a[2]<=zLevel && b[2]<=zLevel) { pA=c; pB1=a; pB2=b; }
        else if (a[2]<zLevel && b[2]>=zLevel && c[2]>=zLevel) { pA=a; pB2=b; pB1=c; }
        else if (b[2]<zLevel && c[2]>=zLevel && a[2]>=zLevel) { pA=b; pB2=c; pB1=a; }
        else if (c[2]<zLevel && a[2]>=zLevel && b[2]>=zLevel) { pA=c; pB2=a; pB1=b; }
        else 
            { return null; }; // Logically, this should not happen, but if it did, not a big deal.

        // This is the proportion of distance to appropriate pB vertex from pA.
        float alpha1 = (zLevel - pA[2]) / (pB1[2] - pA[2]);
        float alpha2 = (zLevel - pA[2]) / (pB2[2] - pA[2]);

        Pair3D pair = new Pair3D();
        pair.a.x = pA[0] + alpha1 * (pB1[0] - pA[0]);
        pair.b.x = pA[0] + alpha2 * (pB2[0] - pA[0]);
        pair.a.y = pA[1] + alpha1 * (pB1[1] - pA[1]);
        pair.b.y = pA[1] + alpha2 * (pB2[1] - pA[1]);
        pair.a.z = zLevel;
        pair.b.z = zLevel;

        return pair;
    }

    Triangle3D inverse3x3() // This method returns an inverse of a 
    {                       // 3x3 matrix ((a[0],a[1],a[2]),(b[0],b[1],b[2]),(c[0],c[1],c[2]))
        // NOTE, THIS CODE IS UNTESTED!!!
        Triangle3D t = new Triangle3D();
        t.a[0] =      b[2]*c[1] - b[1]*c[2];
        t.b[0] =      b[0]*c[2] - b[2]*c[0];
        t.c[0] =      b[1]*c[0] - b[0]*c[1];
        float det = a[0]*t.a[0] + a[1]*t.b[0] + a[2]*t.c[0];
        if (det != 0.f) {
            t.a[0] /= det;
            t.b[0] /= det;
            t.c[0] /= det;
            t.a[1] = (c[2]*a[1] - c[1]*a[2])/det;
            t.b[1] = (c[0]*a[2] - c[2]*a[0])/det;
            t.c[1] = (c[1]*a[0] - c[0]*a[1])/det;
            t.a[2] = (a[2]*b[1] - a[1]*b[2])/det;
            t.b[2] = (a[0]*b[2] - a[2]*b[0])/det;
            t.c[2] = (a[1]*b[0] - a[0]*b[1])/det;
        }

        return t;
    }
    
    Triangle3D multiply3x3(Triangle3D t) // This method returns a product of two 
    {                       // 3x3 matrices ((a[0],a[1],a[2]),(b[0],b[1],b[2]),(c[0],c[1],c[2]))
        // NOTE, THIS CODE IS UNTESTED!!!
        Triangle3D r = new Triangle3D();
        r.a[0] = a[0]*t.a[0] + a[1]*t.b[0] + a[2]*t.c[0];
        r.b[0] = b[0]*t.a[0] + b[1]*t.b[0] + b[2]*t.c[0];
        r.c[0] = c[0]*t.a[0] + c[1]*t.b[0] + c[2]*t.c[0];
        r.a[1] = a[0]*t.a[1] + a[1]*t.b[1] + a[2]*t.c[1];
        r.b[1] = b[0]*t.a[1] + b[1]*t.b[1] + b[2]*t.c[1];
        r.c[1] = c[0]*t.a[1] + c[1]*t.b[1] + c[2]*t.c[1];
        r.a[2] = a[0]*t.a[2] + a[1]*t.b[2] + a[2]*t.c[2];
        r.b[2] = b[0]*t.a[2] + b[1]*t.b[2] + b[2]*t.c[2];
        r.c[2] = c[0]*t.a[2] + c[1]*t.b[2] + c[2]*t.c[2];

        return r;
    }

    public static void main0(String[] args) {
        // Unit test
        Triangle3D m1 = new Triangle3D(
                1,2,1,
                0,0,1,
                1,1,0 );
        Vector3D   v1 = m1.new Vector3D(0,2,1);
        Vector3D   v2 = v1.solve(m1);
        System.out.println(v2.print()); // v1 used as a row vector
        Vector3D   v3 = v1.solveT(m1);
        System.out.println(v3.print()); // v1 used as a column vector
        
        Vector3D p0 = m1.new Vector3D(1,1,-1);
        Vector3D v0 = m1.new Vector3D(0,0,5);
        Vector3D p1 = m1.new Vector3D(0,0,0);
        Vector3D vA = m1.new Vector3D(3,0,0);
        Vector3D vB = m1.new Vector3D(0,1.5f,0);
        if ( p0.isCollision(v0, p1, vA, v1) ) System.out.println("isCollision() worked!");
        else                                  System.out.println("isCollision() failed!");
        if ( p0.isCollision(v0, p1, vA, vB) ) System.out.println("isCollision() worked!");
        else                                  System.out.println("isCollision() failed!");
        if ( p0.isCollision(v0, p1, v1, vB) ) System.out.println("isCollision() failed!");
        else                                  System.out.println("isCollision() worked!");
    }
}
