import com.sun.jna.*;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public class JnaLoadDll {
    static jnaInterface instance;
    static Kernel32 kernel32;
    static {
        instance = (jnaInterface) Native.loadLibrary("kernel32", jnaInterface.class);
        kernel32 = (Kernel32) Native.loadLibrary(Kernel32.class, W32APIOptions.UNICODE_OPTIONS);

    }
    public interface jnaInterface extends StdCallLibrary {
        Pointer VirtualAllocEx(WinNT.HANDLE hProcess,
                               Pointer lpAddress,
                               int dwSize,
                               int flAllocationType,
                               int flProtect);

        boolean CreateProcess(java.lang.String lpApplicationName,
                              java.lang.String lpCommandLine,
                              WinBase.SECURITY_ATTRIBUTES lpProcessAttributes,
                              WinBase.SECURITY_ATTRIBUTES lpThreadAttributes,
                              boolean bInheritHandles,
                              WinDef.DWORD dwCreationFlags,
                              Pointer lpEnvironment,
                              java.lang.String lpCurrentDirectory,
                              WinBase.STARTUPINFO lpStartupInfo,
                              WinBase.PROCESS_INFORMATION lpProcessInformation);

        WinNT.HANDLE CreateRemoteThread(WinNT.HANDLE hProcess,
                                        WinBase.SECURITY_ATTRIBUTES lpThreadAttributes,
                                        int dwStackSize,
                                        Pointer lpStartAddress,
                                        Pointer lpParameter,
                                        int dwCreationFlags,
                                        WinDef.DWORDByReference lpThreadId);
        boolean WriteProcessMemory(WinNT.HANDLE hProcess,
                                   Pointer lpBaseAddress,
                                   Pointer lpBuffer,
                                   int nSize,
                                   IntByReference lpNumberOfBytesWritten);

        Pointer GetProcAddress(WinDef.HMODULE hmodule,
                       int ordinal);
        WinDef.HMODULE LoadLibraryEx(java.lang.String lpFileName,
                                     WinNT.HANDLE hFile,
                                     int flags);
        WinNT.HANDLE GetCurrentProcess();
    }

    public void demo() {
//        byte shellcode[] = new byte[]{(byte)0xc2,(byte)0x14,(byte)0x00}; //32
        byte shellcode[] = new byte[]{(byte)0xc3}; //64
        int shellcodelength =  shellcode.length;
        WinDef.HMODULE hmodule  = kernel32.LoadLibraryEx("jvm.dll",null,0);
        Pointer Functionpointer = instance.GetProcAddress(hmodule,2775);
        WinNT.HANDLE handle = instance.GetCurrentProcess();
        Memory memory = new Memory((long) shellcodelength);
        for (int j = 0; j < shellcodelength; ++j) {
            memory.setByte((long) j, shellcode[j]);
        }
        instance.WriteProcessMemory(handle,Functionpointer,memory,shellcodelength,null);


    }
}