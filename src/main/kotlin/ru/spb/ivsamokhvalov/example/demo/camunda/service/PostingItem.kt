package ru.spb.ivsamokhvalov.example.demo.camunda.service

import org.springframework.stereotype.Service
import ru.spb.ivsamokhvalov.example.demo.camunda.repo.ItemEntity
import ru.spb.ivsamokhvalov.example.demo.camunda.repo.ItemRepository

interface PostingItemService {
    fun createPostingItems(postingId: Long, currency: CurrencyCode, items: Collection<CreateItemsRequest>)
    fun getItemByPostingId(postingId: Long): List<Item>
    fun updateItem(request: UpdateItemRequest)
}

@Service
class PostingItemServiceImpl(
    private val itemRepository: ItemRepository,
) : PostingItemService {

    override fun createPostingItems(postingId: Long, currency: CurrencyCode, items: Collection<CreateItemsRequest>) {
        items.onEach { item ->
            itemRepository.save(
                ItemEntity(
                    skuId = item.skuId,
                    qty = item.qty,
                    postingId = postingId,
                    _originalPrice = item.price,
                    _originalCurrency = currency
                )
            )
        }
    }

    override fun getItemByPostingId(postingId: Long): List<Item> =
        itemRepository.findByPostingIdOrderByItemIdAsc(postingId)

    override fun updateItem(request: UpdateItemRequest) {
        val item = itemRepository.findById(request.itemId).get()
        request.currency?.let { newPrice ->
            item._price = newPrice.price
            item._currency = newPrice.currency
        }
        itemRepository.save(item)
    }
}