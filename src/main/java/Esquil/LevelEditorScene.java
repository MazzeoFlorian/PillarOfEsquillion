package Esquil;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15C;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

public class LevelEditorScene extends Scene {

   //Vertex shader source
    private String vertShadSrc = "#version 330 core\n" +
           "layout (location=0) in vec3 aPos;\n" +
           "layout (location=1) in vec4 aColor;\n" +
           "\n" +
           "out vec4 fColor;\n" +
           "\n" +
           "void main(){\n" +
           "    fColor = aColor;\n" +
           "    gl_Position = vec4(aPos, 1.0);\n" +
           "}";

    //Fragment shader source
    private String fragShadSrc = "#version 330 core\n" +
            "\n" +
            "in vec4 fColor;\n" +
            "\n" +
            "out vec4 color;\n" +
            "\n" +
            "void main(){\n" +
            "    color = fColor;\n" +
            "}";

    private int vertexID, fragmentID, shaderProgram;


    private float[] vertexArray = {
        //position                  //color
        0.5f,   -0.5f,   0.0f,      1.0f,0.0f,0.0f,1.0f, //bottom right
        -0.5f,  0.5f,    0.0f,      0.0f,1.0f,0.0f,1.0f, //Top left
        0.5f,   0.5f,    0.0f,      0.0f,0.0f,1.0f,1,0f, // Top right
        -0.5f,  -0.5f,   0.0f,      1.0f,1.0f,0.0f,1.0f, // bottom left
    };

    //IMPORTANT: IN COUNTER-CLOCKWISE ORDER!
    private int[] elementArray = {
            2, 1, 0, //Top Right tri
            0, 1, 3, //Bottom left tri
    };

    private int vaoID, vboID, eboID;

    public LevelEditorScene() {

    }

    @Override
    public void init(){
        /*
        Compiling and linking the vertex and fragment shaders
        */

        // Load and compile vertex shader
        vertexID = glCreateShader(GL_VERTEX_SHADER);

        //Pass shader source to GPU
        glShaderSource(vertexID, vertShadSrc);
        glCompileShader(vertexID);

        //Check for errors whilst compiling
        int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);
        if (success == GL_FALSE){
            int len = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: 'defaultShader.glsl'\n\tVertex shader compilation failed.");
            System.out.println(glGetShaderInfoLog(vertexID, len));
            assert false : "";
        }

        /*
        Compiling and linking the vertex and fragment shaders
        */

        // Load and compile vertex shader
        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);

        //Pass shader source to GPU
        glShaderSource(fragmentID, fragShadSrc);
        glCompileShader(fragmentID);

        //Check for errors whilst compiling
        success = glGetShaderi(fragmentID, GL_COMPILE_STATUS);
        if (success == GL_FALSE){
            int len = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: 'defaultShader.glsl'\n\tFragment shader compilation failed.");
            System.out.println(glGetShaderInfoLog(fragmentID, len));
            assert false : "";
        }

        // Link shaders & check for errors
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexID);
        glAttachShader(shaderProgram, fragmentID);
        glLinkProgram(shaderProgram);

        //Check: Linking errors
        success = glGetProgrami(shaderProgram, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int len = glGetProgrami(shaderProgram, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: 'defaultShader.glsl'\n\tLinking shaders failed.");
            System.out.println(glGetShaderInfoLog(shaderProgram, len));
            assert false : "";
        }

            /*
            Generating VAO, VBO, EBO buffer objects & send them to the GPU
             */
            vaoID = glGenVertexArrays();
            glBindVertexArray(vaoID);

            //Create a float buffer of vertices
            FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
            vertexBuffer.put(vertexArray).flip();

            //Create the VBO upload to vertex buffer
            vboID = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

            //Create indices & upload
            IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
            elementBuffer.put(elementArray).flip();

            eboID = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER,elementBuffer, GL_STATIC_DRAW);

            //Add vertex attribute pointers
            int positionsSize = 3;
            int colorSize = 4;
            int floatSizeBytes = 4;
            int vertexSizeBytes = (positionsSize + colorSize) * floatSizeBytes;

            glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
            glEnableVertexAttribArray(0);

            glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionsSize * floatSizeBytes);
            glEnableVertexAttribArray(1);
        }


    @Override
    public void update(float dt) {
        //Bind shader program
        glUseProgram(shaderProgram);
        //Bind VAO
        glBindVertexArray(vaoID);

        //Enable vertex attrib pointers
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);

        //Unbind everything
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        glBindVertexArray(0);

        glUseProgram(0);
    }
}
