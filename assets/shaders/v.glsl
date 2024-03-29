
#version 140

/**
-------------- bloom vertex shader -------------
    author: Richman Stewart
    simple vertex shader that sets the position
    to the specified matrix and position while
    passing the vertex colour and tex coords
    to the fragment shader
**/

in vec2 a_position;
in vec2 a_tex_coord;
in vec4 a_colour;

uniform mat4 u_projTrans;

out vec4 v_colour;
out vec2 tex_coord;

void main() {
   v_colour = a_colour;
   tex_coord = a_tex_coord;
   gl_Position = u_projTrans * vec4(a_position, 0, 1);
}
