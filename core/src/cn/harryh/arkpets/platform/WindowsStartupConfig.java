/** Copyright (c) 2022-2024, Harry Huang, Litwak913
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.platform;

import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.Unknown;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;

import java.io.File;
import java.io.FileNotFoundException;


public class WindowsStartupConfig extends StartupConfig {
    private boolean available;
    private File startupDir;
    private File startupFile;

    public WindowsStartupConfig() {
        try {
            this.startupDir = new File(System.getProperty("user.home") + "/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup");
            if (!this.startupDir.isDirectory())
                throw new FileNotFoundException("No such directory " + startupDir.getAbsolutePath());
            if (!new File("ArkPets.exe").exists())
                throw new FileNotFoundException("Executable not found.");
            this.startupFile = new File(startupDir.getAbsolutePath(), "ArkPetsStartup.lnk");
            this.available = true;
        } catch (Exception e) {
            this.startupDir = null;
            this.startupFile = null;
            this.available = false;
            Logger.error("Config", "Auto-startup config may be unavailable, details see below.", e);
        }
    }

    @Override
    public boolean addStartup() {
        if (!this.available) return false;
        try {
            IShellLink lnk = IShellLink.create();
            IPersistFile pf = lnk.getPF();
            String cd = System.getProperty("user.dir");
            cd = cd.replaceAll("\"", "\"\"");
            lnk.SetPath(cd + "\\" + "ArkPets.exe");
            lnk.SetArguments("--direct-start");
            lnk.SetWorkingDirectory(cd);
            pf.Save(startupFile.getAbsolutePath().replaceAll("\"", "\"\""));
            pf.Release();
            lnk.Release();
            return true;
        } catch (Exception e) {
            Logger.error("Config", "Auto-startup adding failed, details see below.", e);
            return false;
        }
    }

    @Override
    public void removeStartup() {
        try {
            IOUtils.FileUtil.delete(startupFile.toPath(), false);
            Logger.info("Config", "Auto-startup was removed: " + startupFile.getAbsolutePath());
        } catch (Exception e) {
            Logger.error("Config", "Auto-startup removing failed, details see below.", e);
        }
    }

    @Override
    public boolean isSetStartup() {
        if (!this.available) return false;
        return startupFile.exists();
    }

    @Override
    public boolean isStartupAvailable() {
        return this.available;
    }

    private static class IPersistFile extends Unknown {
        private IPersistFile(Pointer ptr) {
            super(ptr);
        }

        public void Save(String path) {
            int res = this._invokeNativeInt(6, new Object[]{this.getPointer(), new WString(path), true});
            COMUtils.checkRC(new WinNT.HRESULT(res));
        }
    }

    private static class IShellLink extends Unknown {
        private static final Guid.GUID CLSID_ShellLink = new Guid.GUID("{00021401-0000-0000-c000-000000000046}");
        private static final Guid.GUID IID_IShellLinkW = new Guid.GUID("{000214F9-0000-0000-c000-000000000046}");
        private static final Guid.GUID IID_IPersistFile = new Guid.GUID("{0000010B-0000-0000-c000-000000000046}");

        private IShellLink(Pointer ptr) {
            super(ptr);
        }

        public static IShellLink create() {
            PointerByReference p = new PointerByReference();
            WinNT.HRESULT hr = Ole32.INSTANCE.CoCreateInstance(CLSID_ShellLink, Pointer.NULL, WTypes.CLSCTX_INPROC_SERVER, IID_IShellLinkW, p);
            COMUtils.checkRC(hr);
            return new IShellLink(p.getValue());
        }

        public void SetPath(String path) {
            int res = this._invokeNativeInt(20, new Object[]{this.getPointer(), new WString(path)});
            COMUtils.checkRC(new WinNT.HRESULT(res));
        }

        public void SetWorkingDirectory(String path) {
            int res = this._invokeNativeInt(9, new Object[]{this.getPointer(), new WString(path)});
            COMUtils.checkRC(new WinNT.HRESULT(res));
        }

        public void SetArguments(String arg) {
            int res = this._invokeNativeInt(11, new Object[]{this.getPointer(), new WString(arg)});
            COMUtils.checkRC(new WinNT.HRESULT(res));
        }

        public IPersistFile getPF() {
            PointerByReference p = new PointerByReference();
            WinNT.HRESULT hr = this.QueryInterface(new Guid.REFIID(new Guid.IID(IID_IPersistFile)), p);
            COMUtils.checkRC(hr);
            return new IPersistFile(p.getValue());
        }
    }
}
