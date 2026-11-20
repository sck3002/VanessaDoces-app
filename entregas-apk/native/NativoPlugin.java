package br.com.vanessadoces.entregas;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.util.List;

/* Funções nativas usadas pela tela de Entregas: listar os apps instalados e abrir um deles (o telefone Bluetooth do carro). */
@CapacitorPlugin(name = "Nativo")
public class NativoPlugin extends Plugin {

    @PluginMethod
    public void apps(PluginCall call) {
        PackageManager pm = getContext().getPackageManager();
        Intent principal = new Intent(Intent.ACTION_MAIN, null);
        principal.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> lista = pm.queryIntentActivities(principal, 0);
        JSArray arr = new JSArray();
        for (ResolveInfo ri : lista) {
            JSObject o = new JSObject();
            o.put("nome", String.valueOf(ri.loadLabel(pm)));
            o.put("pacote", ri.activityInfo.packageName);
            arr.put(o);
        }
        JSObject ret = new JSObject();
        ret.put("apps", arr);
        call.resolve(ret);
    }

    @PluginMethod
    public void abrirApp(PluginCall call) {
        String pacote = call.getString("pacote");
        if (pacote == null || pacote.isEmpty()) {
            call.reject("pacote ausente");
            return;
        }
        Intent it = getContext().getPackageManager().getLaunchIntentForPackage(pacote);
        if (it == null) {
            call.reject("app não encontrado");
            return;
        }
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(it);
        call.resolve();
    }
}
