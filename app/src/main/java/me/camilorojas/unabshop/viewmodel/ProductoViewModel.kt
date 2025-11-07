package me.camilorojas.unabshop.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.camilorojas.unabshop.model.Producto
// --- ¡IMPORTANTE! NECESITAMOS ESTE IMPORT ---
import kotlinx.coroutines.tasks.await

class ProductoViewModel : ViewModel() {
    private val TAG = "ProductViewModel"
    private val db = Firebase.firestore //Obtenemos la instancia

    // ESTADO PRODUCTOS
    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos = _productos.asStateFlow()

    // ESTADO DEL FORMULARIO
    var nombre by mutableStateOf("")
    var descripcion by mutableStateOf("")
    var precio by mutableStateOf("")

    init {
        obtenerProductos()
    }

    // ACTUALIZAR ESTADO FORMULARIO
    fun onNameChange(text: String) {
        nombre = text
    }

    fun onDescriptionChange(text: String) {
        descripcion = text
    }

    fun onPriceChange(text: String) {
        if (text.isEmpty() || text.toDoubleOrNull() != null) { // Validación solo números
            precio = text
        }
    }

    //LÓGICA DE FIRESTORE

    //LISTAR OBJETOS
    private fun obtenerProductos() {
        viewModelScope.launch {
            db.collection("productos")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val listaProductos = snapshot.map { doc ->
                            doc.toObject(Producto::class.java).copy(id = doc.id)
                        }
                        _productos.value = listaProductos
                        Log.d(TAG, "Productos actualizados: ${listaProductos.size}")
                    }
                }
        }
    }

    //AGREGAR PRODUCTOS
    fun agregarProducto() {
        val precioDouble = precio.toDoubleOrNull() ?: 0.0
        if (nombre.isBlank() || descripcion.isBlank() || precioDouble == 0.0) {
            Log.w(TAG, "No se puede agregar: campos inválidos")
            return
        }
        val nuevoProducto = Producto(
            nombre = nombre,
            descripcion = descripcion,
            precio = precioDouble
        )

        viewModelScope.launch {
            try {
                val documentReference = db.collection("productos")
                    .add(nuevoProducto)
                    .await()
                Log.d(TAG, "Producto añadido con ID: ${documentReference.id}")

            } catch (e: Exception) {
                Log.w(TAG, "Error añadiendo producto", e)
            }
        }
    }

    //ELIMINAR PRODUCTOS
    fun eliminarProducto(productoId: String) {
        if (productoId.isBlank()) {
            Log.w(TAG, "ID de producto inválido para eliminar")
            return
        }

        viewModelScope.launch {
            try {
                db.collection("productos").document(productoId)
                    .delete()
                    .await()
                Log.d(TAG, "Producto eliminado exitosamente")
            } catch (e: Exception) {
                Log.w(TAG, "Error eliminando producto", e)
            }
        }
    }
}