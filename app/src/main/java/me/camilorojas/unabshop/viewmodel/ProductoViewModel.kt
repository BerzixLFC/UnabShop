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

    // Cuando el ViewModel se inicia, llamamos a obtenerProductos
    init {
        obtenerProductos()
    }

    // ACTUALIZAR ESTADO FORMULARIo
    fun onNameChange(text: String) {
        nombre = text
    }

    fun onDescriptionChange(text: String) {
        descripcion = text
    }

    fun onPriceChange(text: String) {
        if (text.isEmpty() || text.toDoubleOrNull() != null) { // Validación solo números (o un campo vacío)
            precio = text
        }
    }
    
    //LÓGICA DE FIRESTOR
    
    /**
     * 1. LISTAR Productos
     * (Basado en el snippet "Listar productos" de la tarea)
     * Usamos addSnapshotListener para obtener actualizaciones en tiempo real.
     */
    private fun obtenerProductos() {
        // viewModelScope es para corutinas atadas al ciclo de vida del ViewModel
        viewModelScope.launch {
            db.collection("productos")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        // Este es el mapeo clave, como sugiere el snippet de la tarea:
                        // Mapeamos los documentos, convertimos cada uno a objeto Producto
                        // y usamos .copy(id = doc.id) para insertar manualmente el ID.
                        val listaProductos = snapshot.map { doc ->
                            doc.toObject(Producto::class.java).copy(id = doc.id)
                        }
                        // Actualizamos nuestro StateFlow, lo que refrescará la UI
                        _productos.value = listaProductos
                        Log.d(TAG, "Productos actualizados: ${listaProductos.size}")
                    }
                }
        }
    }

    //AGREGAR PRODUCTOS
    
    fun agregarProducto() {
        val precioDouble = precio.toDoubleOrNull() ?: 0.0

        // Validación simple
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
            db.collection("productos")
                .add(nuevoProducto)
                .addOnSuccessListener {
                    Log.d(TAG, "Producto añadido con ID: ${it.id}")
                    // Limpiamos el formulario después de agregar
                    nombre = ""
                    descripcion = ""
                    precio = ""
                }
                .addOnFailureListener { e ->
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
            db.collection("productos").document(productoId)
                .delete()
                .addOnSuccessListener {
                    Log.d(TAG, "Producto eliminado exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error eliminando producto", e)
                }
        }
    }
}