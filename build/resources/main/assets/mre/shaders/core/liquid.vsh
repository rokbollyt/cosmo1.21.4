#version 150

#moj_import <mre:common.glsl>

in vec3 Position;   // позиция вершины
in vec4 Color;      // цвет вершины

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 FragCoord; // экранные координаты фрагмента
out vec2 TexCoord;  // UV для текстуры
out vec4 FragColor; // цвет для фрагмента

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    // Получаем координаты фрагмента из gl_VertexID (как в common.glsl)
    FragCoord = rvertexcoord(gl_VertexID);

    // Текстурные координаты из нормализованной позиции
    TexCoord = gl_Position.xy * 0.5 + 0.5;

    FragColor = Color;
}
