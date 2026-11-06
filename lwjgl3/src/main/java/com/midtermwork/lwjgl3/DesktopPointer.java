package com.midtermwork.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.midtermwork.Main;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;
import org.lwjgl.system.windows.POINT;
import org.lwjgl.system.windows.RECT;
import org.lwjgl.system.windows.User32;

/** Maps physical cursor pixels through the actual client rectangle before HUD projection. */
public final class DesktopPointer implements Main.PointerReader {
    private final double[] cursorX=new double[1],cursorY=new double[1];
    private final long physicalCursor,screenToClient,clientRect;
    public DesktopPointer(){
        if(Platform.get()==Platform.WINDOWS){
            physicalCursor=User32.getLibrary().getFunctionAddress("GetPhysicalCursorPos");
            screenToClient=User32.getLibrary().getFunctionAddress("ScreenToClient");
            clientRect=User32.getLibrary().getFunctionAddress("GetClientRect");
        }else physicalCursor=screenToClient=clientRect=0;
    }
    @Override public void read(float[] position){
        long window=((Lwjgl3Graphics)Gdx.graphics).getWindow().getWindowHandle();
        if(physicalCursor!=0&&screenToClient!=0&&clientRect!=0){
            long previous=User32.SetThreadDpiAwarenessContext(User32.DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE_V2);
            try(MemoryStack stack=MemoryStack.stackPush()){
                long hwnd=GLFWNativeWin32.glfwGetWin32Window(window);
                POINT cursor=POINT.calloc(stack);RECT rect=RECT.calloc(stack);
                if(JNI.callPI(cursor.address(),physicalCursor)!=0&&JNI.callPPI(hwnd,cursor.address(),screenToClient)!=0&&JNI.callPPI(hwnd,rect.address(),clientRect)!=0&&rect.right()>0&&rect.bottom()>0){
                    position[0]=cursor.x()*(float)Gdx.graphics.getWidth()/rect.right();
                    position[1]=cursor.y()*(float)Gdx.graphics.getHeight()/rect.bottom();return;
                }
            }finally{if(previous!=0)User32.SetThreadDpiAwarenessContext(previous);}
        }
        GLFW.glfwGetCursorPos(window,cursorX,cursorY);
        position[0]=(float)cursorX[0];position[1]=(float)cursorY[0];
    }
}